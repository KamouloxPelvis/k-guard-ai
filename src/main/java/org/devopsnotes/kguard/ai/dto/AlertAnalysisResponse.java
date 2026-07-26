package org.devopsnotes.kguard.ai.dto;

import java.util.List;

public record AlertAnalysisResponse(
        String correlationId,
        String source,
        String severity,
        String incidentType,
        String humanSummary,
        String riskLevel,
        String sanitizedLog,
        Double confidenceScore,
        List<String> recommendedActions
) {
}
