package org.devopsnotes.kguard.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record NormalizedAlertRequest(
        @NotBlank String source,
        @NotNull @Valid Event event,
        Map<String, Object> metadata
) {
    public record Event(
            @NotBlank String title,
            @NotBlank String severity,
            @NotBlank String rawLog,
            String eventId,
            String host,
            String workload,
            String category
    ) {
    }
}
