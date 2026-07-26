package org.devopsnotes.kguard.ai.sanitizer;

import org.springframework.stereotype.Component;

@Component
public class AlertSanitizer {

    public String sanitize(String rawLog) {
        if (rawLog == null || rawLog.isBlank()) {
            return "";
        }

        String sanitized = rawLog;

        sanitized = sanitized.replaceAll("(?i)(password\\s*[=:]\\s*)([^\\s,;]+)", "$1[REDACTED]");
        sanitized = sanitized.replaceAll("(?i)(token\\s*[=:]\\s*)([^\\s,;]+)", "$1[REDACTED]");
        sanitized = sanitized.replaceAll("(?i)(apikey\\s*[=:]\\s*)([^\\s,;]+)", "$1[REDACTED]");
        sanitized = sanitized.replaceAll("(?i)(api_key\\s*[=:]\\s*)([^\\s,;]+)", "$1[REDACTED]");
        sanitized = sanitized.replaceAll("(?i)(secret\\s*[=:]\\s*)([^\\s,;]+)", "$1[REDACTED]");
        sanitized = sanitized.replaceAll("(?i)(authorization\\s*:\\s*bearer\\s+)([^\\s]+)", "$1[REDACTED]");

        return sanitized;
    }
}
