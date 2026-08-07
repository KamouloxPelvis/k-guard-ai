package org.devopsnotes.kguard.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kguard.ai.backend")
public record KguardBackendProperties(
        boolean enabled,
        String url,
        String eventsPath
) {
}
