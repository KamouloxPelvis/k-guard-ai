package org.devopsnotes.kguard.ai.llm.dto;

public record OllamaChatMessage(
        String role,
        String content
) {
}
