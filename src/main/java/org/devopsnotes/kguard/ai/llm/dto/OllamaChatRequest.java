package org.devopsnotes.kguard.ai.llm.dto;

import java.util.List;
import java.util.Map;

public record OllamaChatRequest(
        String model,
        Boolean stream,
        Boolean think,
        Object format,
        Map<String, Object> options,
        List<OllamaChatMessage> messages
) {
}
