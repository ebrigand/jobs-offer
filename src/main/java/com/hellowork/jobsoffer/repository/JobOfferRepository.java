package com.hellowork.jobsoffer.repository;

import com.hellowork.jobsoffer.model.JobOffer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface JobOfferRepository extends ReactiveMongoRepository<JobOffer, String> {
    Mono<JobOffer> findByExternalId(String externalId);

    Flux<JobOffer> findByCity(String city);

}