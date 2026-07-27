package org.devopsnotes.kguard.ai.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.devopsnotes.kguard.ai.config.ElasticsearchProperties;
import org.devopsnotes.kguard.ai.dto.AlertAnalysisDocument;
import org.devopsnotes.kguard.ai.dto.AlertAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Service
@ConditionalOnProperty(prefix = "kguard.ai.elasticsearch", name = "export-enabled", havingValue = "true")
public class ElasticsearchAlertExportService {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchAlertExportService.class);

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchProperties properties;

    public ElasticsearchAlertExportService(
            ElasticsearchClient elasticsearchClient,
            ElasticsearchProperties properties
    ) {
        this.elasticsearchClient = elasticsearchClient;
        this.properties = properties;
    }

    public void export(AlertAnalysisResponse response) {
        AlertAnalysisDocument document = new AlertAnalysisDocument(
                response.correlationId(),
                response.source(),
                response.severity(),
                response.incidentType(),
                response.humanSummary(),
                response.riskLevel(),
                response.sanitizedLog(),
                response.confidenceScore(),
                response.recommendedActions(),
                response.llmEnrichment(),
                Instant.now()
        );

        try {
            elasticsearchClient.index(i -> i
                    .index(properties.index())
                    .id(response.correlationId())
                    .document(document)
            );
            log.info("Exported alert analysis to Elasticsearch with correlationId={}", response.correlationId());
        } catch (IOException e) {
            log.error("Failed to export alert analysis to Elasticsearch with correlationId={}", response.correlationId(), e);
        }
    }
}
