# Changelog

All notable changes to this project will be documented in this file.

## [0.3.0] - 2026-07-26

### Added
- Added correlation IDs to alert analysis responses.
- Added confidence scoring for alert classification.
- Added a response structure ready for future local LLM integration.
- Added clearer incident classification categories for security event enrichment.

### Changed
- Improved the alert analysis service to generate more traceable and structured output.
- Refined the response payload to support future LLMOps processing and downstream observability.
- Kept the current pipeline fully local, with sanitized input and human-readable French output.

### Security
- Preserved log sanitization before any future model handoff.
- Continued to avoid exposing secrets, tokens, or credentials in analysis output.

### Validation
- Verified local Spring Boot startup on port 8080.
- Verified valid alert analysis requests return correlation IDs, sanitized logs, and confidence scores.
- Verified invalid requests still return structured validation errors.

## [0.2.0] - 2026-07-26

### Added
- Added request validation for the alert analysis API.
- Added sensitive-data sanitization for incoming raw logs.
- Added structured validation error responses for invalid API requests.
- Added French human-readable incident summaries for analyzed alerts.
- Added initial incident discrimination logic for runtime execution, privilege escalation, sensitive data exposure, and endpoint security events.

### Changed
- Improved the alert analysis response to include the sanitized log content.
- Improved API behavior so invalid payloads now return structured field-level validation errors.
- Refined the current MVP toward a safer LLMOps-oriented processing pipeline before any future LLM integration.

### Security
- Masked simple secrets and credentials patterns such as password, token, api key, and authorization bearer values before analysis output.
- Kept the processing model aligned with K-Guard security principles by preventing sensitive values from being exposed in returned analysis data.

### Validation
- Verified local Spring Boot startup on port 8080.
- Verified valid alert analysis requests return sanitized output.
- Verified invalid requests return HTTP 400 with explicit field validation details.

## [0.1.0] - 2026-07-26

### Added
- Initial Spring Boot bootstrap for the K-Guard AI microservice.
- Added the first REST endpoint for alert analysis.
- Added DTO-based request and response handling.
- Added initial incident classification and recommended action generation.
- Added local development and test execution with Maven Wrapper.
