package org.devopsnotes.kguard.ai.dto;

import java.util.List;

public record LlmEnrichment(
        String model,
        String verdict,
        String analystSummary,
        List<String> investigationSteps,
        List<String> iocs,
        List<String> hypotheses
) {
}
