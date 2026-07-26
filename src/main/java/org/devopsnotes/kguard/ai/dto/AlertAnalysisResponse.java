package org.devopsnotes.kguard.ai.dto;

import java.util.List;

public record AlertAnalysisResponse(
        String source,
        String severity,
        String incidentType,
        String humanSummary,
        String riskLevel,
        String sanitizedLog,
        List<String> recommendedActions
) {
}
