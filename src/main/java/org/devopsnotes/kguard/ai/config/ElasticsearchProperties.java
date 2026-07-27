package org.devopsnotes.kguard.ai.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kguard.ai.elasticsearch")
@Validated
public record ElasticsearchProperties(
        boolean exportEnabled,
        @NotBlank String url,
        @NotBlank String index
) {
}
