package com.hellowork.jobsoffer.controller;

import com.hellowork.jobsoffer.report.ReportService;
import com.hellowork.jobsoffer.service.JobOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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