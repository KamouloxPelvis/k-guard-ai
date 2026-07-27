package org.devopsnotes.kguard.ai.llm;

import org.devopsnotes.kguard.ai.config.LlmProperties;
import org.devopsnotes.kguard.ai.llm.dto.OllamaChatMessage;
import org.devopsnotes.kguard.ai.llm.dto.OllamaChatRequest;
import org.devopsnotes.kguard.ai.llm.dto.OllamaChatResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {

    private final RestClient restClient;
    private final LlmProperties properties;

    public OllamaClient(RestClient ollamaRestClient, LlmProperties properties) {
        this.restClient = ollamaRestClient;
        this.properties = properties;
    }

    public String chatForJson(String userPrompt, Map<String, Object> jsonSchema) {
        OllamaChatRequest request = new OllamaChatRequest(
                properties.model(),
                false,
                false,
                jsonSchema,
                Map.of("temperature", 0),
                List.of(new OllamaChatMessage("user", userPrompt))
        );

        OllamaChatResponse response = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OllamaChatResponse.class);

        if (response == null || response.message() == null || response.message().content() == null) {
            throw new IllegalStateException("Empty response from Ollama");
        }

        return response.message().content();
    }
}
