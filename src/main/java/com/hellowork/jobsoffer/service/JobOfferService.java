package com.hellowork.jobsoffer.service;

import com.hellowork.jobsoffer.model.JobOffer;
import reactor.core.publisher.Flux;

public interface JobOfferService {

    Flux<JobOffer> fetchForCity(String city);

    Flux<JobOffer> fetchAndStoreAllPagesForCitiesAndDepartment(String cities, String departments);
}
