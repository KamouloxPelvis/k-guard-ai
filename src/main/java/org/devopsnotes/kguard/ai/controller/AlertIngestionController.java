package org.devopsnotes.kguard.ai.controller;

import jakarta.validation.Valid;
import org.devopsnotes.kguard.ai.dto.AlertAnalysisResponse;
import org.devopsnotes.kguard.ai.dto.AlertRequest;
import org.devopsnotes.kguard.ai.dto.NormalizedAlertRequest;
import org.devopsnotes.kguard.ai.service.AlertAnalysisService;
import org.devopsnotes.kguard.ai.service.AlertNormalizationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingest")
public class AlertIngestionController {

    private final AlertNormalizationService alertNormalizationService;
    private final AlertAnalysisService alertAnalysisService;

    public AlertIngestionController(
            AlertNormalizationService alertNormalizationService,
            AlertAnalysisService alertAnalysisService
    ) {
        this.alertNormalizationService = alertNormalizationService;
        this.alertAnalysisService = alertAnalysisService;
    }

    @PostMapping("/normalized")
    public AlertAnalysisResponse ingestNormalized(@Valid @RequestBody NormalizedAlertRequest request) {
        AlertRequest normalizedAlert = alertNormalizationService.normalize(request);
        return alertAnalysisService.analyze(normalizedAlert);
    }
}
