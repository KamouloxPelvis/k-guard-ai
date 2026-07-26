# Changelog

All notable changes to this project will be documented in this file.

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
