package com.hellowork.jobsoffer.it;

import com.hellowork.jobsoffer.model.JobOffer;
import com.hellowork.jobsoffer.poleemploi.PoleEmploiClient;
import com.hellowork.jobsoffer.report.ReportService;
import com.hellowork.jobsoffer.repository.JobOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
class JobOfferServiceTest {

    @Autowired
    private com.hellowork.jobsoffer.service.JobOfferService jobOfferService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private JobOfferRepository jobOfferRepository;

    @MockBean
    private PoleEmploiClient poleEmploiClient;

    private JobOffer offer1;
    private JobOffer offer2;

    @BeforeEach
    void setup() {
        // Vide la base avant chaque test
        jobOfferRepository.deleteAll().block();

        // Création des offres communes à tous les tests
        offer1 = JobOffer.builder()
                .externalId("1")
                .company("Capgemini")
                .description("Développeur Java")
                .city("Bordeaux")
                .build();

        offer2 = JobOffer.builder()
                .externalId("2")
                .company("Hellowork")
                .description("Développeur C#")
                .city("Rennes")
                .build();

        // Mock de l’API Pôle Emploi pour toutes les pages
        Mockito.when(poleEmploiClient.fetchAndSaveOffers(Mockito.anyString(), Mockito.anyString(), Mockito.eq(0)))
                .thenReturn(Flux.just(offer1));
        Mockito.when(poleEmploiClient.fetchAndSaveOffers(Mockito.anyString(), Mockito.anyString(), Mockito.eq(1)))
                .thenReturn(Flux.just(offer2));
        Mockito.when(poleEmploiClient.fetchAndSaveOffers(Mockito.anyString(), Mockito.anyString(), Mockito.intThat(p -> p >= 2)))
                .thenReturn(Flux.empty());
    }

    @Test
    void testInsertionAndReadWithMongo() {
        StepVerifier.create(jobOfferService.fetchAndStoreAllPagesForCitiesAndDepartment("33063", "75").take(2))
                .expectNextMatches(o -> o.getExternalId().equals(offer1.getExternalId()))
                .expectNextMatches(o -> o.getExternalId().equals(offer2.getExternalId()))
                .verifyComplete();

        StepVerifier.create(jobOfferRepository.findByExternalId(offer1.getExternalId()))
                .expectNextMatches(o -> o.getDescription().equals(offer1.getDescription()))
                .verifyComplete();

        StepVerifier.create(jobOfferRepository.findByExternalId(offer2.getExternalId()))
                .expectNextMatches(o -> o.getDescription().equals(offer2.getDescription()))
                .verifyComplete();
    }

    @Test
    void testImportAndGenerateReport() {
        StepVerifier.create(jobOfferService.fetchAndStoreAllPagesForCitiesAndDepartment("33063", "75").take(2))
                .expectNextMatches(o -> o.getExternalId().equals(offer1.getExternalId()))
                .expectNextMatches(o -> o.getExternalId().equals(offer2.getExternalId()))
                .verifyComplete();

        StepVerifier.create(jobOfferRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(reportService.generateReport())
                .assertNext(report -> {
                    // Vérifie que le rapport contient les deux entreprises
                    assert report.contains(offer1.getCompany());
                    assert report.contains(offer2.getCompany());
                })
                .verifyComplete();
    }
}
