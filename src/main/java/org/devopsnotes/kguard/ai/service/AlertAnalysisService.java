package org.devopsnotes.kguard.ai.service;

import org.devopsnotes.kguard.ai.dto.AlertAnalysisResponse;
import org.devopsnotes.kguard.ai.dto.AlertRequest;
import org.devopsnotes.kguard.ai.dto.LlmEnrichment;
import org.devopsnotes.kguard.ai.llm.provider.LlmProviderRouter;
import org.devopsnotes.kguard.ai.sanitizer.AlertSanitizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AlertAnalysisService {

    private final AlertSanitizer alertSanitizer;
    private final LlmProviderRouter llmProviderRouter;
    private final ElasticsearchAlertExportService elasticsearchAlertExportService;

    public AlertAnalysisService(
            AlertSanitizer alertSanitizer,
            LlmProviderRouter llmProviderRouter,
            org.springframework.beans.factory.ObjectProvider<ElasticsearchAlertExportService> elasticsearchAlertExportServiceProvider
    ) {
        this.alertSanitizer = alertSanitizer;
        this.llmProviderRouter = llmProviderRouter;
        this.elasticsearchAlertExportService = elasticsearchAlertExportServiceProvider.getIfAvailable();
    }

    public AlertAnalysisResponse analyze(AlertRequest request) {
        String correlationId = UUID.randomUUID().toString();
        String sanitizedLog = alertSanitizer.sanitize(request.rawLog());
        String incidentType = classifyIncident(sanitizedLog);
        String riskLevel = mapRiskLevel(request.severity());
        Double confidenceScore = calculateConfidenceScore(incidentType, sanitizedLog);
        String summary = buildSummary(request.source(), request.title(), incidentType, riskLevel);

        LlmEnrichment llmEnrichment = llmProviderRouter.enrich(
                request.source(),
                request.title(),
                request.severity(),
                incidentType,
                riskLevel,
                sanitizedLog
        );

        AlertAnalysisResponse response = new AlertAnalysisResponse(
                correlationId,
                request.source(),
                request.severity(),
                incidentType,
                summary,
                riskLevel,
                sanitizedLog,
                confidenceScore,
                buildActions(incidentType),
                llmEnrichment
        );

        if (elasticsearchAlertExportService != null) {
            elasticsearchAlertExportService.export(response);
        }

        return response;
    }

    private String classifyIncident(String raw) {
        String log = raw == null ? "" : raw.toLowerCase();

        if (log.contains("shell") || log.contains("bash") || log.contains("sh ")) {
            return "runtime-execution";
        }
        if (log.contains("root") || log.contains("privilege")) {
            return "privilege-escalation";
        }
        if (log.contains("token") || log.contains("secret") || log.contains("apikey") || log.contains("api_key")) {
            return "sensitive-data-exposure";
        }
        if (log.contains("wazuh")) {
            return "endpoint-security-event";
        }
        return "unknown";
    }

    private String mapRiskLevel(String severity) {
        if (severity == null) {
            return "medium";
        }
        return switch (severity.toLowerCase()) {
            case "critical", "high" -> "high";
            case "medium" -> "medium";
            case "low" -> "low";
            default -> "medium";
        };
    }

    private Double calculateConfidenceScore(String incidentType, String sanitizedLog) {
        if ("runtime-execution".equals(incidentType) && sanitizedLog.toLowerCase().contains("bash")) return 0.95;
        if ("privilege-escalation".equals(incidentType)) return 0.85;
        if ("sensitive-data-exposure".equals(incidentType)) return 0.90;
        if ("endpoint-security-event".equals(incidentType)) return 0.80;
        return 0.60;
    }

    private String buildSummary(String source, String title, String incidentType, String riskLevel) {
        return switch (incidentType) {
            case "runtime-execution" ->
                    "Interactive shell execution was detected from source " + source +
                    ". The event \"" + title + "\" suggests potentially dangerous activity inside a container. " +
                    "The estimated risk level is " + riskLevel + ".";
            case "privilege-escalation" ->
                    "Behavior related to privilege escalation was detected from source " + source +
                    ". The event \"" + title + "\" requires immediate verification. " +
                    "The estimated risk level is " + riskLevel + ".";
            case "sensitive-data-exposure" ->
                    "Sensitive content was detected in the log sent by " + source +
                    ". The event \"" + title + "\" was sanitized before processing. " +
                    "The estimated risk level is " + riskLevel + ".";
            case "endpoint-security-event" ->
                    "An endpoint security event related to " + source +
                    " was identified. The event \"" + title + "\" should be correlated with compliance and inventory data. " +
                    "The estimated risk level is " + riskLevel + ".";
            default ->
                    "A security alert from " + source +
                    " was analyzed. The event \"" + title + "\" requires analyst review. " +
                    "The estimated risk level is " + riskLevel + ".";
        };
    }

    private List<String> buildActions(String incidentType) {
        return switch (incidentType) {
            case "runtime-execution" -> List.of(
                    "Isolate the affected pod or workload.",
                    "Review related Falco events and Kubernetes logs.",
                    "Confirm whether the shell activity was authorized."
            );
            case "privilege-escalation" -> List.of(
                    "Identify the account, pod, or process behind the escalation.",
                    "Review Kubernetes permissions and security contexts.",
                    "Launch a targeted compromise assessment."
            );
            case "sensitive-data-exposure" -> List.of(
                    "Confirm that sensitive data has been masked.",
                    "Trace the origin of the exposure in the application flow.",
                    "Prevent any reuse of potentially compromised secrets."
            );
            case "endpoint-security-event" -> List.of(
                    "Correlate the event with Wazuh inventory data.",
                    "Check the agent status and latest reported events.",
                    "Assess endpoint impact before remediation."
            );
            default -> List.of(
                    "Inspect the originating resource.",
                    "Correlate with nearby security events.",
                    "Triage the alert before remediation."
            );
        };
    }
}
