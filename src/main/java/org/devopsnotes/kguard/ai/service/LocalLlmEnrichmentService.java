package org.devopsnotes.kguard.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.devopsnotes.kguard.ai.config.LlmProperties;
import org.devopsnotes.kguard.ai.dto.LlmEnrichment;
import org.devopsnotes.kguard.ai.llm.OllamaClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LocalLlmEnrichmentService {

    private final OllamaClient ollamaClient;
    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;

    public LocalLlmEnrichmentService(
            OllamaClient ollamaClient,
            LlmProperties llmProperties,
            ObjectMapper objectMapper
    ) {
        this.ollamaClient = ollamaClient;
        this.llmProperties = llmProperties;
        this.objectMapper = objectMapper;
    }

    public LlmEnrichment enrich(
            String source,
            String title,
            String severity,
            String incidentType,
            String riskLevel,
            String sanitizedLog
    ) {
        if (!llmProperties.enabled() || !"ollama".equalsIgnoreCase(llmProperties.provider())) {
            return null;
        }

        try {
            String schemaAsText = """
                    {
                      "type": "object",
                      "properties": {
                        "verdict": {
                          "type": "string"
                        },
                        "analystSummary": {
                          "type": "string"
                        },
                        "investigationSteps": {
                          "type": "array",
                          "items": { "type": "string" }
                        },
                        "iocs": {
                          "type": "array",
                          "items": { "type": "string" }
                        },
                        "hypotheses": {
                          "type": "array",
                          "items": { "type": "string" }
                        }
                      },
                      "required": [
                        "verdict",
                        "analystSummary",
                        "investigationSteps",
                        "iocs",
                        "hypotheses"
                      ]
                    }
                    """;

            String prompt = """
                    You are a SOC/DevSecOps analyst for K-Guard AI.

                    Return only a valid JSON object that strictly matches the provided schema.

                    JSON schema:
                    %s

                    Alert context:
                    - source: %s
                    - title: %s
                    - severity: %s
                    - incidentType: %s
                    - riskLevel: %s
                    - sanitizedLog: %s

                    Constraints:
                    - verdict must be a short analyst qualification.
                    - analystSummary must be 1 to 3 sentences maximum.
                    - investigationSteps must contain at most 3 concrete actions.
                    - iocs may be empty if no IOC can be identified.
                    - hypotheses must contain 1 to 3 realistic hypotheses.
                    - Respond in English.
                    - Do not repeat the same sentence across multiple fields.
                    - Keep the output concise, technical, and actionable.
                    """.formatted(
                    schemaAsText,
                    safe(source),
                    safe(title),
                    safe(severity),
                    safe(incidentType),
                    safe(riskLevel),
                    safe(sanitizedLog)
            );

            Map<String, Object> schema = Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "verdict", Map.of("type", "string"),
                            "analystSummary", Map.of("type", "string"),
                            "investigationSteps", Map.of(
                                    "type", "array",
                                    "items", Map.of("type", "string")
                            ),
                            "iocs", Map.of(
                                    "type", "array",
                                    "items", Map.of("type", "string")
                            ),
                            "hypotheses", Map.of(
                                    "type", "array",
                                    "items", Map.of("type", "string")
                            )
                    ),
                    "required", List.of(
                            "verdict",
                            "analystSummary",
                            "investigationSteps",
                            "iocs",
                            "hypotheses"
                    )
            );

            String rawJson = ollamaClient.chatForJson(prompt, schema);
            JsonNode root = objectMapper.readTree(rawJson);

            return new LlmEnrichment(
                    llmProperties.model(),
                    root.path("verdict").asText("unknown"),
                    root.path("analystSummary").asText("No LLM summary available."),
                    toStringList(root.path("investigationSteps")),
                    toStringList(root.path("iocs")),
                    toStringList(root.path("hypotheses"))
            );
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> toStringList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(item -> values.add(item.asText()));
        }
        return values;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
