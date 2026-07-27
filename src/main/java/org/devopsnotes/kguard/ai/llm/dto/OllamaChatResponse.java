package org.devopsnotes.kguard.ai.llm.dto;

public record OllamaChatResponse(
        String model,
        String created_at,
        OllamaChatMessage message,
        Boolean done,
        String done_reason
) {
}
