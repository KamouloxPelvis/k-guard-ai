package org.devopsnotes.kguard.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kguard.ai.elasticsearch")
public record ElasticsearchProperties(
        boolean exportEnabled,
        String url,
        String index
) {
}
