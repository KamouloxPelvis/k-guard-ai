package org.devopsnotes.kguard.ai.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.devopsnotes.kguard.ai.dto.AlertAnalysisResponse;
import org.devopsnotes.kguard.ai.dto.NormalizedAlertRequest;
import org.devopsnotes.kguard.ai.dto.AlertRequest;
import org.devopsnotes.kguard.ai.service.AlertAnalysisService;
import org.devopsnotes.kguard.ai.service.AlertNormalizationService;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/falco")
public class FalcoIngestionController {

    private final AlertNormalizationService normalizationService;
    private final AlertAnalysisService analysisService;

    @PostMapping("/event")
    public AlertAnalysisResponse ingestFalcoEvent(@RequestBody Map<String, Object> falcoPayload) {
        log.info("Received Falco alert payload: {}", falcoPayload);
        NormalizedAlertRequest normalized = buildNormalizedFromFalco(falcoPayload);
        AlertRequest alert = normalizationService.normalize(normalized);
        return analysisService.analyze(alert);
    }

    @SuppressWarnings("unchecked")
    private NormalizedAlertRequest buildNormalizedFromFalco(Map<String, Object> falcoPayload) {
        // Champs de base
        String source = (String) falcoPayload.getOrDefault("source", "falco");
        String rule = (String) falcoPayload.getOrDefault("rule", "Falco alert");
        String priorityRaw = (String) falcoPayload.getOrDefault("priority", "INFO");
        String output = (String) falcoPayload.getOrDefault("output", "");
        String hostname = (String) falcoPayload.getOrDefault("hostname", null);
        String time = (String) falcoPayload.getOrDefault("time", null);

        // output_fields (Map)
        Map<String, Object> outputFields = null;
        Object ofObj = falcoPayload.get("output_fields");
        if (ofObj instanceof Map) {
            outputFields = (Map<String, Object>) ofObj;
        }

        // Extraire workload (pod, container, namespace)
        String workload = null;
        if (outputFields != null) {
            String pod = (String) outputFields.get("k8s.pod.name");
            String ns = (String) outputFields.get("k8s.ns.name");
            String container = (String) outputFields.get("container.name");

            if (pod != null && ns != null) {
                workload = ns + "/" + pod;
            } else if (container != null) {
                workload = container;
            }
        }

        // Normaliser severity
        String severity = normalizeFalcoPriority(priorityRaw);

        // Event
        NormalizedAlertRequest.Event event = new NormalizedAlertRequest.Event(
                rule,          // title
                severity,      // severity (INFO/WARNING/ERROR/CRITICAL)
                output,        // rawLog
                time,          // eventId
                hostname,      // host
                workload,      // workload
                "falco"        // category
        );

        // Metadata enrichi
        Map<String, Object> metadata = Map.of(
                "rawPayload", falcoPayload,
                "tags", falcoPayload.getOrDefault("tags", List.of()),
                "sourceType", falcoPayload.getOrDefault("source", "falco")
        );

        return new NormalizedAlertRequest(
                source,
                event,
                metadata
        );
    }

    private String normalizeFalcoPriority(String priorityRaw) {
        if (priorityRaw == null) {
            return "INFO";
        }
        switch (priorityRaw) {
            case "Emergency":
            case "Alert":
            case "Critical":
            case "Error":
                return "ERROR";
            case "Warning":
                return "WARNING";
            case "Notice":
            case "Informational":
            default:
                return "INFO";
        }
    }
}