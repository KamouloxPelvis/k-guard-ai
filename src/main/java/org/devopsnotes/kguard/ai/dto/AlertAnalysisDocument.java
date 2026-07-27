package org.devopsnotes.kguard.ai.dto;

import java.time.Instant;
import java.util.List;

public record AlertAnalysisDocument(
        String correlationId,
        String source,
        String severity,
        String incidentType,
        String humanSummary,
        String riskLevel,
        String sanitizedLog,
        Double confidenceScore,
        List<String> recommendedActions,
        LlmEnrichment llmEnrichment,
        Instant exportedAt
) {
}
