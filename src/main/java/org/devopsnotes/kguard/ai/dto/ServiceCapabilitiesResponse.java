package org.devopsnotes.kguard.ai.dto;

import java.util.List;

public record ServiceCapabilitiesResponse(
        String service,
        String version,
        String defaultLanguage,
        int maxRawLogLength,
        boolean includeSanitizedLogInResponse,
        boolean llmEnabled,
        String llmProvider,
        boolean elasticsearchExportEnabled,
        List<String> supportedSources,
        List<String> supportedProfiles,
        List<String> activeFeatures
) {
}
