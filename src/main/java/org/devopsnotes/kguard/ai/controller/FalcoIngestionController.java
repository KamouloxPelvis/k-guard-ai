package org.devopsnotes.kguard.ai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.devopsnotes.kguard.ai.dto.AlertAnalysisResponse;
import org.devopsnotes.kguard.ai.dto.NormalizedAlertRequest;
import org.devopsnotes.kguard.ai.dto.AlertRequest;
import org.devopsnotes.kguard.ai.service.AlertAnalysisService;
import org.devopsnotes.kguard.ai.service.AlertNormalizationService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/falco")
public class FalcoIngestionController {

    private final AlertNormalizationService normalizationService;
    private final AlertAnalysisService analysisService;

    public FalcoIngestionController(AlertNormalizationService normalizationService,
                                    AlertAnalysisService analysisService) {
        this.normalizationService = normalizationService;
        this.analysisService = analysisService;
    }

    @PostMapping("/event")
    public AlertAnalysisResponse ingestFalcoEvent(@RequestBody Map<String, Object> falcoPayload) {
        log.info("Received Falco alert: {}", falcoPayload);
        NormalizedAlertRequest normalized = buildNormalizedFromFalco(falcoPayload);
        AlertRequest alert = normalizationService.normalize(normalized);
        return analysisService.analyze(alert);
    }

    private NormalizedAlertRequest buildNormalizedFromFalco(Map<String, Object> falcoPayload) {
        // Champs de base du JSON Falco
        String source = (String) falcoPayload.getOrDefault("source", "falco");
        String rule = (String) falcoPayload.getOrDefault("rule", "Falco alert");
        String priority = (String) falcoPayload.getOrDefault("priority", "INFO");
        String output = (String) falcoPayload.getOrDefault("output", "");

        String hostname = (String) falcoPayload.getOrDefault("hostname", null);
        String time = (String) falcoPayload.getOrDefault("time", null);

        // Construire l'Event interne
        NormalizedAlertRequest.Event event = new NormalizedAlertRequest.Event(
                rule,          // title
                priority,      // severity
                output,        // rawLog
                time,          // eventId (on peut mettre l'horodatage, à affiner)
                hostname,      // host
                null,          // workload (à extraire éventuellement de output_fields)
                "falco"        // category
        );

        // Mettre le payload brut dans metadata pour référence
        Map<String, Object> metadata = Map.of(
                "rawPayload", falcoPayload
        );

        // Retourner un NormalizedAlertRequest complet
        return new NormalizedAlertRequest(
                source,
                event,
                metadata
        );
    }
}