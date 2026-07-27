package org.devopsnotes.kguard.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kguard.ai.llm")
public record LlmProperties(
        boolean enabled,
        String provider,
        String baseUrl,
        String model,
        int timeoutSeconds
) {
}
