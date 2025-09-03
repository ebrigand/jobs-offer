package com.hellowork.jobsoffer.report;

import reactor.core.publisher.Mono;

public interface ReportService {

    Mono<String> generateReport();


}