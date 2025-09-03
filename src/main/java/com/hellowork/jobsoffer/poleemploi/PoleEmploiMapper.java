package com.hellowork.jobsoffer.poleemploi;

import com.hellowork.jobsoffer.model.JobOffer;

import java.time.Instant;
import java.util.Map;

public class PoleEmploiMapper {
    public static JobOffer mapToJobOffer(Map<String, Object> raw) {
        Map<String, Object> entreprise = (Map<String, Object>) raw.get("entreprise");
        Map<String, Object> lieuTravail = (Map<String, Object>) raw.get("lieuTravail");
        Map<String, Object> contact = (Map<String, Object>) raw.get("contact");

        return JobOffer.builder()
                .externalId((String) raw.get("id"))
                .title((String) raw.get("intitule"))
                .company(entreprise != null ? (String) entreprise.get("nom") : null)
                .contractType((String) raw.get("typeContrat"))
                .description((String) raw.get("description"))
                .applyUrl(contact != null ? (String) contact.get("urlPostulation") : null)
                .city(lieuTravail != null ? (String) lieuTravail.get("libelle") : null)
                .country("France")
                .fetchedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
