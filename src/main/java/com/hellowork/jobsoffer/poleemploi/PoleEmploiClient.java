package com.hellowork.jobsoffer.poleemploi;

import com.hellowork.jobsoffer.model.JobOffer;
import reactor.core.publisher.Flux;

public interface PoleEmploiClient {
    Flux<JobOffer> fetchAndSaveOffers(String cities, String departments, int page);
}
