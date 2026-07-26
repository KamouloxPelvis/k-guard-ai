package org.devopsnotes.kguard.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AlertRequest(
        @NotBlank(message = "source is required")
        String source,

        @NotBlank(message = "title is required")
        String title,

        @NotBlank(message = "severity is required")
        String severity,

        @NotBlank(message = "rawLog is required")
        String rawLog
) {
}
