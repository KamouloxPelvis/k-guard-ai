package org.devopsnotes.kguard.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.devopsnotes.kguard.ai.dto.AlertRequest;
import org.devopsnotes.kguard.ai.dto.NormalizedAlertRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertNormalizationService {

    public AlertRequest normalize(NormalizedAlertRequest request) {
        log.info("Normalizing alert: source={}, title={}, severity={}, host={}, workload={}",
                request.source(), request.event().title(), request.event().severity(),
                request.event().host(), request.event().workload());

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

        String rawLog = rawLogBuilder.toString();

        AlertRequest result = new AlertRequest(
                normalizedSource,
                request.event().title(),
                normalizedSeverity,
                rawLog
        );

        log.info("Normalized alert: source={}, title={}, severity={}, rawLog={}",
                result.source(), result.title(), result.severity(), result.rawLog());

        return result;
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