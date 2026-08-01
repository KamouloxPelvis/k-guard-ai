package org.devopsnotes.kguard.ai.llm.provider;

import lombok.extern.slf4j.Slf4j;
import org.devopsnotes.kguard.ai.dto.LlmEnrichment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OllamaLlmProvider implements LlmProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String providerName() {
        return "ollama";
    }

    @Override
    public boolean isAvailable() {
        String enabledEnv = System.getenv("KGUARD_AI_LLM_ENABLED");
        String providerEnv = System.getenv("KGUARD_AI_LLM_PROVIDER");
        String baseUrlEnv = System.getenv("KGUARD_AI_LLM_BASE_URL");
        String modelEnv = System.getenv("KGUARD_AI_LLM_MODEL");

        boolean enabled = enabledEnv != null && enabledEnv.equalsIgnoreCase("true");
        boolean isOllama = providerEnv != null && providerEnv.equalsIgnoreCase("ollama");
        boolean hasBaseUrl = baseUrlEnv != null && !baseUrlEnv.isBlank();
        boolean hasModel = modelEnv != null && !modelEnv.isBlank();

        boolean available = enabled && isOllama && hasBaseUrl && hasModel;

        if (!available) {
            log.warn("OllamaLlmProvider not available: enabledEnv={}, providerEnv={}, baseUrlEnv={}, modelEnv={}",
                    enabledEnv, providerEnv, baseUrlEnv, modelEnv);
        }

        return available;
    }

    @Override
    public LlmEnrichment enrich(
            String source,
            String title,
            String severity,
            String incidentType,
            String riskLevel,
            String sanitizedLog
    ) {
        String baseUrl = System.getenv("KGUARD_AI_LLM_BASE_URL");
        String model = System.getenv("KGUARD_AI_LLM_MODEL");

        String prompt = buildPrompt(source, title, severity, incidentType, riskLevel, sanitizedLog);

        log.info("Calling Ollama LLM at {} with model {} via /api/chat", baseUrl, model);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", prompt
            );

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(message),
                    "stream", false
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    baseUrl + "/api/chat",
                    requestEntity,
                    Map.class
            );

            if (response == null) {
                log.warn("Ollama returned null response");
                return fallbackEnrichment(model, "No response from Ollama");
            }

            Object messageObj = response.get("message");
            String text = "";

            if (messageObj instanceof Map<?, ?> messageMap) {
                Object content = messageMap.get("content");
                if (content != null) {
                    text = content.toString();
                }
            }

            log.info("Ollama raw response (chat): {}", text);

            return new LlmEnrichment(
                    model,
                    "LLM_ANALYSIS",
                    text,
                    List.of(),
                    List.of(),
                    List.of()
            );
        } catch (Exception e) {
            log.error("Error while calling Ollama LLM via /api/chat", e);
            return fallbackEnrichment(model, "Error calling Ollama: " + e.getMessage());
        }
    }

    private LlmEnrichment fallbackEnrichment(String model, String message) {
        return new LlmEnrichment(
                model,
                "LLM_FALLBACK",
                message,
                List.of(),
                List.of(),
                List.of()
        );
    }

    private String buildPrompt(String source,
                               String title,
                               String severity,
                               String incidentType,
                               String riskLevel,
                               String sanitizedLog) {
        return """
            You are a SOC analyst AI. Analyze the following security alert.

            Source: %s
            Title: %s
            Severity: %s
            Incident type: %s
            Risk level: %s

            Sanitized log:
            %s

            Provide a concise analysis in plain text. Focus on:
            - What this alert likely means.
            - How serious it is.
            - What an analyst should look at next.
            """.formatted(source, title, severity, incidentType, riskLevel, sanitizedLog);
    }
}