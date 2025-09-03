package com.hellowork.jobsoffer.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "offers")
public class JobOffer {
    @Id
    private String id;

    @Indexed(unique = true)
    private String externalId;

    private String title;
    private String company;
    private String contractType;
    private String description;
    private String applyUrl;
    private String city;
    private String country;

    private Instant fetchedAt;
    private Instant updatedAt;
}