package com.hellowork.jobsoffer.report;

import com.hellowork.jobsoffer.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final JobOfferRepository jobOfferRepository;

    /**
     * Build a textual report by collecting offers and grouping.
     * For moderate DB sizes this is fine; for huge DB use aggregation pipeline.
     */
    public Mono<String> generateReport() {
        return jobOfferRepository.findAll()
                .collectList()
                .map(list -> {
                    Map<String, Long> byContract = list.stream()
                            .collect(Collectors.groupingBy(o -> nullToEmpty(o.getContractType()), Collectors.counting()));
                    Map<String, Long> byCompany = list.stream()
                            .collect(Collectors.groupingBy(o -> nullToEmpty(o.getCompany()), Collectors.counting()));
                    Map<String, Long> byCountry = list.stream()
                            .collect(Collectors.groupingBy(o -> nullToEmpty(o.getCountry()), Collectors.counting()));

                    StringBuilder sb = new StringBuilder();
                    sb.append("=== Rapport Offres ===\n");
                    sb.append("Par contrat:\n").append(byContract).append("\n\n");
                    sb.append("Par entreprise:\n").append(byCompany).append("\n\n");
                    sb.append("Par pays:\n").append(byCountry).append("\n\n");
                    return sb.toString();
                });
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}