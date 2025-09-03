package com.hellowork.jobsoffer.unit;

import com.hellowork.jobsoffer.model.JobOffer;
import com.hellowork.jobsoffer.poleemploi.PoleEmploiClient;
import com.hellowork.jobsoffer.repository.JobOfferRepository;
import com.hellowork.jobsoffer.service.JobOfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class JobOfferServiceTest {

    private JobOfferService jobOfferService;
    private JobOfferRepository jobOfferRepository;
    private PoleEmploiClient poleEmploiClient;

    @BeforeEach
    void setup() {
        jobOfferRepository = Mockito.mock(JobOfferRepository.class);
        poleEmploiClient = Mockito.mock(PoleEmploiClient.class);
        jobOfferService = new JobOfferService(poleEmploiClient, jobOfferRepository); // constructeur avec dépendances

        // Offre test
        JobOffer offer1 = JobOffer.builder()
                .externalId("1")
                .title("Développeur Java")
                .city("Bordeaux")
                .build();

        JobOffer offer2 = JobOffer.builder()
                .externalId("2")
                .title("Développeur React")
                .city("Rennes")
                .build();

        // Mock fetchAndSaveOffers
        Mockito.when(poleEmploiClient.fetchAndSaveOffers(Mockito.anyString(), Mockito.anyString(), Mockito.eq(0)))
                .thenReturn(Flux.just(offer1));
        Mockito.when(poleEmploiClient.fetchAndSaveOffers(Mockito.anyString(), Mockito.anyString(), Mockito.eq(1)))
                .thenReturn(Flux.just(offer2));
        Mockito.when(poleEmploiClient.fetchAndSaveOffers(Mockito.anyString(), Mockito.anyString(), Mockito.intThat(p -> p >= 2)))
                .thenReturn(Flux.empty());

        // Mock repository
        Mockito.when(jobOfferRepository.findByExternalId(Mockito.anyString()))
                .thenReturn(Mono.empty()); // toutes nouvelles offres
        Mockito.when(jobOfferRepository.save(Mockito.any()))
                .thenAnswer(i -> Mono.just(i.getArgument(0))); // retourne l'objet sauvegardé
    }

    @Test
    void testFetchAndStoreAllPages_multipleOffers() {
        StepVerifier.create(
                        jobOfferService.fetchAndStoreAllPagesForCitiesAndDepartment("33063", "75")
                                .take(2) // on prend seulement 2 offres pour le test
                )
                .expectNextMatches(o -> o.getExternalId().equals("1"))
                .expectNextMatches(o -> o.getExternalId().equals("2"))
                .verifyComplete();

        // Vérifie que save a été appelé 2 fois
        Mockito.verify(jobOfferRepository, Mockito.times(2)).save(Mockito.any());
    }
}
