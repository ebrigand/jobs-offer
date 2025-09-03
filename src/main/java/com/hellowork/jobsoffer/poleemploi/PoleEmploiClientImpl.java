package com.hellowork.jobsoffer.poleemploi;

import com.hellowork.jobsoffer.model.JobOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class PoleEmploiClientImpl implements PoleEmploiClient {

    private final WebClient apiClient;
    private final WebClient authClient;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final int pageSize;
    private final AtomicReference<String> token = new AtomicReference<>(null);
    private volatile Instant tokenExpiry = Instant.EPOCH;

    public PoleEmploiClientImpl(
            @Value("${pole.emploi.api-base}") String apiBase,
            @Value("${pole.emploi.token-base}") String tokenBase,
            @Value("${pole.emploi.client-id}") String clientId,
            @Value("${pole.emploi.client-secret}") String clientSecret,
            @Value("${pole.emploi.scope}") String scope,
            @Value("${pole.emploi.page-size}") Integer pageSize
    ) {
        this.apiClient = WebClient.builder().baseUrl(apiBase).build();
        this.authClient = WebClient.builder().baseUrl(tokenBase).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.pageSize = pageSize;
    }

    private Mono<String> getToken() {
        String current = token.get();
        if (current != null && Instant.now().isBefore(tokenExpiry.minusSeconds(30))) {
            log.debug("Token encore valide (expiry: {}).", tokenExpiry);
            return Mono.just(current);
        }
        log.info("Token expiré ou absent, récupération d’un nouveau...");
        return authClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/connexion/oauth2/access_token")
                        .queryParam("realm", "/partenaire")
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials&scope=" + scope)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .retrieve()
                .bodyToMono(Map.class)
                .map(m -> {
                    String t = (String) m.get("access_token");
                    Integer expiresIn = (Integer) m.getOrDefault("expires_in", 3600);
                    token.set(t);
                    tokenExpiry = Instant.now().plusSeconds(expiresIn);
                    log.info("✅ Nouveau token récupéré, expiry dans {}s (à {}).", expiresIn, tokenExpiry);
                    return t;
                })
                .doOnError(err -> log.error("❌ Erreur lors de la récupération du token: {}", err.getMessage(), err));
    }

    /**
     * Fetch offers for a given page (page index starting at 0) and pagesize.
     * Returns the parsed JSON as Map (caller will extract "resultats").
     */
    public Flux<JobOffer> fetchAndSaveOffers(String cities, String departments, int page) {
        log.debug("Fetching offers: cities={}, departements={}, page={}, pageSize={}", cities, departments, page, pageSize);

        return getToken()
                .flatMapMany(token ->
                        apiClient.get()
                                .uri(uriBuilder -> uriBuilder
                                        .path("/offresdemploi/v2/offres/search")
                                        .queryParam("commune", cities)
                                        .queryParam("distance", "0")
                                        .queryParam("department", departments)
                                        .queryParam("range", page * pageSize + "-" + ((page + 1) * pageSize - 1))
                                        .build())
                                .headers(h -> h.setBearerAuth(token))
                                .exchangeToFlux(response -> {
                                    if (response.statusCode().is2xxSuccessful()) {
                                        return response.bodyToMono(PoleEmploiResponse.class)
                                                .flatMapMany(resp -> Flux.fromIterable(resp.getResultats()))
                                                .map(PoleEmploiMapper::mapToJobOffer);
                                    } else {
                                        return response.createException().flatMapMany(Flux::error);
                                    }
                                })
                );
    }
}
