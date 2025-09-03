package com.hellowork.jobsoffer.service;

import com.hellowork.jobsoffer.exception.TooManyCitiesException;
import com.hellowork.jobsoffer.model.JobOffer;
import com.hellowork.jobsoffer.poleemploi.PoleEmploiClient;
import com.hellowork.jobsoffer.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobOfferServiceImpl implements JobOfferService{

    private final PoleEmploiClient poleEmploiClient;
    private final JobOfferRepository jobOfferRepository;
    private static final int MAX_DB_WRITE_CONCURRENCY = 10;

    /**
     * Vérifie si l'offre existe déjà (via externalId).
     * Si oui → mise à jour, sinon → insertion.
     */
    private Mono<JobOffer> saveOrUpdateOffer(JobOffer newOffer) {
        return jobOfferRepository.findByExternalId(newOffer.getExternalId())
                .flatMap(existing -> {
                    log.debug("🔄 Mise à jour offre existante [id={} title={} city={}]",
                            existing.getExternalId(), existing.getTitle(), existing.getCity());

                    existing.setTitle(newOffer.getTitle());
                    existing.setCompany(newOffer.getCompany());
                    existing.setContractType(newOffer.getContractType());
                    existing.setDescription(newOffer.getDescription());
                    existing.setApplyUrl(newOffer.getApplyUrl());
                    existing.setCity(newOffer.getCity());
                    existing.setCountry(newOffer.getCountry());
                    existing.setUpdatedAt(Instant.now());
                    return jobOfferRepository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("➕ Insertion nouvelle offre [id={} title={} city={}]",
                            newOffer.getExternalId(), newOffer.getTitle(), newOffer.getCity());

                    newOffer.setFetchedAt(Instant.now());
                    newOffer.setUpdatedAt(Instant.now());
                    return jobOfferRepository.save(newOffer);
                }));
    }

    public Flux<JobOffer> fetchForCity(String city) {
        log.info("📍 Récupération des offres déjà en DB pour la ville: {}", city);
        return jobOfferRepository.findByCity(city);
    }

    public Flux<JobOffer> fetchAndStoreAllPagesForCitiesAndDepartment(String cities, String departments) {
        long count = cities.chars().filter(c -> c == ',').count();
        if(count > 4) throw new TooManyCitiesException("Le maximum de villes est de 5, cities: " + cities);

        log.info("🏙️ Début import offres pour la ville: {}", cities);

        return Flux.range(0, Integer.MAX_VALUE) // simulateur de pagination infini
                .concatMap(page ->
                        poleEmploiClient.fetchAndSaveOffers(cities, departments, page)
                                .collectList()
                                .flatMapMany(offers -> {
                                    if (offers.isEmpty()) {
                                        log.info("⏹️ Fin des offres pour les villes: {} departements: {} (page: {})", cities, departments, page);
                                        return Flux.empty(); // stop si aucune offre
                                    }
                                    log.info("📄 Villes={} Departements={} Page={} → {} offres récupérées", cities, departments, page, offers.size());
                                    return Flux.fromIterable(offers)
                                            .flatMap(this::saveOrUpdateOffer, MAX_DB_WRITE_CONCURRENCY);
                                })
                )
                .takeUntilOther(Mono.delay(Duration.ofSeconds(10))) // sécurité anti-boucle infinie
                .doOnComplete(() -> log.info("✅ Import terminé pour les villes: {} et les départements: {}", cities, departments));
    }
}
