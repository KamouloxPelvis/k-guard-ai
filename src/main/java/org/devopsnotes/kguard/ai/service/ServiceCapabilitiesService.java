package org.devopsnotes.kguard.ai.service;

import org.devopsnotes.kguard.ai.config.ElasticsearchProperties;
import org.devopsnotes.kguard.ai.config.KguardAiProperties;
import org.devopsnotes.kguard.ai.config.LlmProperties;
import org.devopsnotes.kguard.ai.dto.ServiceCapabilitiesResponse;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceCapabilitiesService {

    private final KguardAiProperties kguardAiProperties;
    private final LlmProperties llmProperties;
    private final ElasticsearchProperties elasticsearchProperties;
    private final BuildProperties buildProperties;

    public ServiceCapabilitiesService(
            KguardAiProperties kguardAiProperties,
            LlmProperties llmProperties,
            ElasticsearchProperties elasticsearchProperties,
            BuildProperties buildProperties
    ) {
        this.kguardAiProperties = kguardAiProperties;
        this.llmProperties = llmProperties;
        this.elasticsearchProperties = elasticsearchProperties;
        this.buildProperties = buildProperties;
    }

    public ServiceCapabilitiesResponse getCapabilities() {
        List<String> activeFeatures = new ArrayList<>();
        activeFeatures.add("deterministic-analysis");
        activeFeatures.add("sanitization");
        activeFeatures.add("risk-scoring");
        activeFeatures.add("recommended-actions");
        activeFeatures.add("normalized-ingestion");

        if (llmProperties.enabled()) {
            activeFeatures.add("llm-enrichment");
        }

        if (elasticsearchProperties.exportEnabled()) {
            activeFeatures.add("elasticsearch-export");
        }

        return new ServiceCapabilitiesResponse(
                "k-guard-ai",
                buildProperties.getVersion(),
                kguardAiProperties.defaultLanguage(),
                kguardAiProperties.maxRawLogLength(),
                kguardAiProperties.includeSanitizedLogInResponse(),
                llmProperties.enabled(),
                llmProperties.provider(),
                elasticsearchProperties.exportEnabled(),
                List.of("falco", "wazuh", "kguard", "generic", "fluent-bit", "elasticsearch"),
                List.of("local", "vps", "kubernetes"),
                activeFeatures
        );
    }
}
