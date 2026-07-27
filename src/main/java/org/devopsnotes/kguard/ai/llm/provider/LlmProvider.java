package org.devopsnotes.kguard.ai.llm.provider;

import org.devopsnotes.kguard.ai.dto.LlmEnrichment;

public interface LlmProvider {
    String providerName();
    boolean isAvailable();

    LlmEnrichment enrich(
            String source,
            String title,
            String severity,
            String incidentType,
            String riskLevel,
            String sanitizedLog
    );
}
