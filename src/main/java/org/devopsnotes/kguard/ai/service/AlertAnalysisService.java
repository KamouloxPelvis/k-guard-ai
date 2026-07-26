package org.devopsnotes.kguard.ai.service;

import org.devopsnotes.kguard.ai.dto.AlertAnalysisResponse;
import org.devopsnotes.kguard.ai.dto.AlertRequest;
import org.devopsnotes.kguard.ai.sanitizer.AlertSanitizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AlertAnalysisService {

    private final AlertSanitizer alertSanitizer;

    public AlertAnalysisService(AlertSanitizer alertSanitizer) {
        this.alertSanitizer = alertSanitizer;
    }

    public AlertAnalysisResponse analyze(AlertRequest request) {
        String correlationId = UUID.randomUUID().toString();
        String sanitizedLog = alertSanitizer.sanitize(request.rawLog());
        String incidentType = classifyIncident(sanitizedLog);
        String riskLevel = mapRiskLevel(request.severity());
        Double confidenceScore = calculateConfidenceScore(incidentType, sanitizedLog);
        String summary = buildSummary(request.source(), request.title(), incidentType, riskLevel);

        return new AlertAnalysisResponse(
                correlationId,
                request.source(),
                request.severity(),
                incidentType,
                summary,
                riskLevel,
                sanitizedLog,
                confidenceScore,
                buildActions(incidentType)
        );
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
                    "Une exécution de shell interactive a été détectée depuis la source " + source +
                    ". L'événement \"" + title + "\" suggère une activité potentiellement dangereuse dans un conteneur. " +
                    "Le niveau de risque estimé est " + riskLevel + ".";
            case "privilege-escalation" ->
                    "Un comportement lié à une élévation de privilèges a été détecté depuis la source " + source +
                    ". L'événement \"" + title + "\" nécessite une vérification immédiate. " +
                    "Le niveau de risque estimé est " + riskLevel + ".";
            case "sensitive-data-exposure" ->
                    "Un élément sensible a été détecté dans le log transmis par " + source +
                    ". L'événement \"" + title + "\" a été assaini avant traitement. " +
                    "Le niveau de risque estimé est " + riskLevel + ".";
            case "endpoint-security-event" ->
                    "Un événement de sécurité endpoint lié à " + source +
                    " a été identifié. L'événement \"" + title + "\" doit être corrélé avec les données de conformité et d'inventaire. " +
                    "Le niveau de risque estimé est " + riskLevel + ".";
            default ->
                    "Une alerte de sécurité provenant de " + source +
                    " a été analysée. L'événement \"" + title + "\" nécessite une revue analyste. " +
                    "Le niveau de risque estimé est " + riskLevel + ".";
        };
    }

    private List<String> buildActions(String incidentType) {
        return switch (incidentType) {
            case "runtime-execution" -> List.of(
                    "Isoler le pod ou la charge de travail concernée.",
                    "Vérifier les événements Falco et les logs Kubernetes associés.",
                    "Confirmer si l'ouverture du shell était autorisée ou non."
            );
            case "privilege-escalation" -> List.of(
                    "Identifier le compte, le pod ou le processus à l'origine de l'élévation.",
                    "Vérifier les droits Kubernetes et les security contexts.",
                    "Lancer une revue de compromission ciblée."
            );
            case "sensitive-data-exposure" -> List.of(
                    "Confirmer que les données sensibles ont été masquées.",
                    "Rechercher l'origine de l'exposition dans la chaîne applicative.",
                    "Empêcher toute réutilisation de secret potentiellement compromis."
            );
            case "endpoint-security-event" -> List.of(
                    "Corréler l'événement avec l'inventaire Wazuh.",
                    "Vérifier le statut de l'agent et les derniers événements remontés.",
                    "Qualifier l'impact endpoint avant action de remédiation."
            );
            default -> List.of(
                    "Inspecter la ressource d'origine.",
                    "Corréler avec les événements de sécurité voisins.",
                    "Qualifier l'alerte avant remédiation."
            );
        };
    }
}
