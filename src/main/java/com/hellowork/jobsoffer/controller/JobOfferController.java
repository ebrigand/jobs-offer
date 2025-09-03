package com.hellowork.jobsoffer.controller;

import com.hellowork.jobsoffer.model.JobOffer;
import com.hellowork.jobsoffer.report.ReportService;
import com.hellowork.jobsoffer.service.JobOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/jobs-offer")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService jobOfferService;
    private final ReportService reportService;

    // fetch all offers for a city (page-by-page)
    @PostMapping("/store")
    public Mono<Void> store() {
        jobOfferService.fetchAndStoreAllPagesForCitiesAndDepartment("35238,33063",  "75").subscribe();
        return Mono.empty();
    }

    // generate textual report
    @GetMapping("/report")
    public Mono<String> report() {
        return reportService.generateReport();
    }
}