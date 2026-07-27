package org.devopsnotes.kguard.ai.llm.provider;

import org.devopsnotes.kguard.ai.config.LlmProperties;
import org.devopsnotes.kguard.ai.dto.LlmEnrichment;
import org.devopsnotes.kguard.ai.service.LocalLlmEnrichmentService;
import org.springframework.stereotype.Component;

@Component
public class OllamaLlmProvider implements LlmProvider {

    private final LlmProperties properties;
    private final LocalLlmEnrichmentService enrichmentService;

    public OllamaLlmProvider(LlmProperties properties, LocalLlmEnrichmentService enrichmentService) {
        this.properties = properties;
        this.enrichmentService = enrichmentService;
    }

    @Override
    public String providerName() {
        return "ollama";
    }

    @Override
    public boolean isAvailable() {
        return properties.enabled() && "ollama".equalsIgnoreCase(properties.provider());
    }

    @Override
    public LlmEnrichment enrich(
            String source,
            String title,
            String severity,
            String incidentType,
            String riskLevel,
            String sanitizedLog
    ) {
        return enrichmentService.enrich(
                source,
                title,
                severity,
                incidentType,
                riskLevel,
                sanitizedLog
        );
    }
}
