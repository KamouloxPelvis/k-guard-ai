package org.devopsnotes.kguard.ai.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kguard.ai")
@Validated
public record KguardAiProperties(
        @Min(256) @Max(20000) int maxRawLogLength,
        @NotBlank String defaultLanguage,
        boolean includeSanitizedLogInResponse
) {
}
