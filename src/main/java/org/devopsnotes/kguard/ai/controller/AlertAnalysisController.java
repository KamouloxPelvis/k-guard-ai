package org.devopsnotes.kguard.ai.controller;

import jakarta.validation.Valid;
import org.devopsnotes.kguard.ai.dto.AlertAnalysisResponse;
import org.devopsnotes.kguard.ai.dto.AlertRequest;
import org.devopsnotes.kguard.ai.service.AlertAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertAnalysisController {

    private final AlertAnalysisService alertAnalysisService;

    public AlertAnalysisController(AlertAnalysisService alertAnalysisService) {
        this.alertAnalysisService = alertAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AlertAnalysisResponse> analyze(@Valid @RequestBody AlertRequest request) {
        return ResponseEntity.ok(alertAnalysisService.analyze(request));
    }
}
