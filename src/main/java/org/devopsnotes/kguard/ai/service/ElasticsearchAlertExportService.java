package org.devopsnotes.kguard.ai.service;

import org.devopsnotes.kguard.ai.config.ElasticsearchProperties;
import org.devopsnotes.kguard.ai.dto.AlertAnalysisResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ElasticsearchAlertExportService {

    private final ElasticsearchProperties properties;
    private final RestTemplate restTemplate;

    public ElasticsearchAlertExportService(
            ElasticsearchProperties properties,
            RestTemplate restTemplate
    ) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    public void export(AlertAnalysisResponse response) {
        if (response == null || !properties.exportEnabled()) {
            return;
        }

        String url = properties.url() + "/" + properties.index() + "/_doc";

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("correlationId", response.correlationId());
        document.put("source", response.source());
        document.put("severity", response.severity());
        document.put("incidentType", response.incidentType());
        document.put("humanSummary", response.humanSummary());
        document.put("riskLevel", response.riskLevel());
        document.put("sanitizedLog", response.sanitizedLog());
        document.put("confidenceScore", response.confidenceScore());
        document.put("recommendedActions", response.recommendedActions());
        document.put("llmEnrichment", response.llmEnrichment());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url, new HttpEntity<>(document, headers), String.class);
    }
}