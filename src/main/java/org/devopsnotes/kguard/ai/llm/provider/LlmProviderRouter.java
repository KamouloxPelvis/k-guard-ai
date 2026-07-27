package org.devopsnotes.kguard.ai.llm.provider;

import org.devopsnotes.kguard.ai.dto.LlmEnrichment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class LlmProviderRouter {

    private final List<LlmProvider> providers;

    public LlmProviderRouter(List<LlmProvider> providers) {
        this.providers = providers;
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
                .filter(Objects::nonNull)
                .filter(provider -> provider.isAvailable())
                .findFirst()
                .map(provider -> provider.enrich(
                        source,
                        title,
                        severity,
                        incidentType,
                        riskLevel,
                        sanitizedLog
                ))
                .orElse(null);
    }
}
