package com.hellowork.jobsoffer.poleemploi;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PoleEmploiResponse {
    private List<Map<String, Object>> resultats;
}