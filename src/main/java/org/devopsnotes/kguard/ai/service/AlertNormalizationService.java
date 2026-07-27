package org.devopsnotes.kguard.ai.service;

import org.devopsnotes.kguard.ai.dto.AlertRequest;
import org.devopsnotes.kguard.ai.dto.NormalizedAlertRequest;
import org.springframework.stereotype.Service;

@Service
public class AlertNormalizationService {

    public AlertRequest normalize(NormalizedAlertRequest request) {
        String normalizedSource = normalizeSource(request.source());
        String normalizedSeverity = normalizeSeverity(request.event().severity());

        StringBuilder rawLogBuilder = new StringBuilder();
        rawLogBuilder.append(request.event().rawLog());

        if (request.event().host() != null && !request.event().host().isBlank()) {
            rawLogBuilder.append(" | host=").append(request.event().host());
        }

        if (request.event().workload() != null && !request.event().workload().isBlank()) {
            rawLogBuilder.append(" | workload=").append(request.event().workload());
        }

        if (request.event().category() != null && !request.event().category().isBlank()) {
            rawLogBuilder.append(" | category=").append(request.event().category());
        }

        if (request.event().eventId() != null && !request.event().eventId().isBlank()) {
            rawLogBuilder.append(" | eventId=").append(request.event().eventId());
        }

        return new AlertRequest(
                normalizedSource,
                request.event().title(),
                normalizedSeverity,
                rawLogBuilder.toString()
        );
    }

    private String normalizeSource(String source) {
        if (source == null) {
            return "generic";
        }

        return switch (source.trim().toLowerCase()) {
            case "falco" -> "falco";
            case "wazuh" -> "wazuh";
            case "kguard", "k-guard" -> "kguard";
            case "fluent-bit", "fluentbit" -> "fluent-bit";
            case "elasticsearch", "elastic", "elk" -> "elasticsearch";
            default -> "generic";
        };
    }

    private String normalizeSeverity(String severity) {
        if (severity == null || severity.isBlank()) {
            return "medium";
        }

        return switch (severity.trim().toLowerCase()) {
            case "emergency", "alert", "critical", "crit", "high" -> "high";
            case "warning", "warn", "medium", "moderate" -> "medium";
            case "info", "informational", "low" -> "low";
            default -> "medium";
        };
    }
}
