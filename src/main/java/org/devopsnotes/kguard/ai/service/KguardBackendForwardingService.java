package org.devopsnotes.kguard.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.devopsnotes.kguard.ai.config.KguardBackendProperties;
import org.devopsnotes.kguard.ai.dto.AlertAnalysisResponse;
import org.devopsnotes.kguard.ai.dto.AlertRequest;
import org.devopsnotes.kguard.ai.dto.LlmEnrichment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class KguardBackendForwardingService {

    private final RestTemplate restTemplate;
    private final KguardBackendProperties properties;

    public KguardBackendForwardingService(
            RestTemplate restTemplate,
            KguardBackendProperties properties
    ) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public void forward(AlertAnalysisResponse response, AlertRequest alert) {
        if (!properties.enabled() || response == null) {
            return;
        }

        String url = properties.url() + properties.eventsPath();
        LlmEnrichment enrichment = response.llmEnrichment();

        Map<String, Object> enrichmentPayload = new LinkedHashMap<>();
        enrichmentPayload.put("model", valueOrNull(enrichment, "model"));
        enrichmentPayload.put("verdict", valueOrNull(enrichment, "verdict"));
        enrichmentPayload.put("analyst_summary", valueOrNull(enrichment, "summary"));
        enrichmentPayload.put("investigation_steps", enrichment == null ? null : enrichment.investigationSteps());
        enrichmentPayload.put("iocs", enrichment == null ? null : enrichment.iocs());
        enrichmentPayload.put("hypotheses", enrichment == null ? null : enrichment.hypotheses());
        enrichmentPayload.put("human_summary", response.humanSummary());
        enrichmentPayload.put("incident_type", response.incidentType());
        enrichmentPayload.put("risk_level", response.riskLevel());
        enrichmentPayload.put("confidence_score", response.confidenceScore());
        enrichmentPayload.put("recommended_actions", response.recommendedActions());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event_id", response.correlationId());
        payload.put("source", "kguard-ai");
        payload.put("rule_name", alert == null ? response.incidentType() : alert.title());
        payload.put("priority", response.severity());
        payload.put("output", response.humanSummary());
        payload.put("ai_status", isFallback(enrichment) ? "fallback" : "enriched");
        payload.put("ai_enrichment", enrichmentPayload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(payload, headers),
                    String.class
            );

            log.info(
                    "Forwarded analyzed alert to K-Guard: correlationId={}, status={}",
                    response.correlationId(),
                    payload.get("ai_status")
            );
        } catch (RestClientException exception) {
            log.warn(
                    "Unable to forward analyzed alert to K-Guard: correlationId={}, error={}",
                    response.correlationId(),
                    exception.getMessage()
            );
        }
    }

    private boolean isFallback(LlmEnrichment enrichment) {
        return enrichment == null
                || "LLM_UNAVAILABLE".equalsIgnoreCase(enrichment.verdict());
    }

    private Object valueOrNull(LlmEnrichment enrichment, String field) {
        if (enrichment == null) {
            return null;
        }

        return switch (field) {
            case "model" -> enrichment.model();
            case "verdict" -> enrichment.verdict();
            case "summary" -> enrichment.analystSummary();
            default -> null;
        };
    }
}
