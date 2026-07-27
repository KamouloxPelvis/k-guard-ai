package org.devopsnotes.kguard.ai.llm.provider;

import org.devopsnotes.kguard.ai.dto.LlmEnrichment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LlmProviderRouter {

    private final List<LlmProvider> providers;

    public LlmProviderRouter(List<LlmProvider> providers) {
        this.providers = providers != null ? providers : Collections.emptyList();
    }

    public LlmEnrichment enrich(
            String source,
            String title,
            String severity,
            String incidentType,
            String riskLevel,
            String sanitizedLog
    ) {
        return providers.stream()
                .filter(provider -> provider.isAvailable())
                .findFirst()
                .map(provider -> provider.enrich(source, title, severity, incidentType, riskLevel, sanitizedLog))
                .orElse(null);
    }
}
