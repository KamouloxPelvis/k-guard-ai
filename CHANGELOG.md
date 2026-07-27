# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Planned work for v0.6.0 will be tracked here.

## [0.5.0] - 2026-07-27

### Added
- LLM provider abstraction with a router-based integration model.
- Ollama provider implementation aligned with the provider abstraction layer.
- Optional Elasticsearch export path for analyzed alerts.
- Production-oriented `Dockerfile` and `.dockerignore`.
- Portable Kubernetes manifests for `ConfigMap`, `Deployment`, `Service`, and example `Secret`.
- Kubernetes deployment notes in `k8s/README.md`.
- GitHub Actions workflow for GitHub Container Registry publishing.
- Immutable Git SHA image tagging in addition to the `latest` tag.

### Changed
- Project documentation now reflects container packaging, GHCR publishing, and Kubernetes deployment flow.
- Alert enrichment architecture is now structured around pluggable providers instead of a direct single-provider coupling.
- Deployment assets are now portable and repository-friendly for external users.

### Security
- Kubernetes deployment manifests now apply baseline hardening with non-root execution, dropped Linux capabilities, and `RuntimeDefault` seccomp.
- Portable manifests avoid committing real secrets and keep sensitive settings external to Git.
- Documentation now explicitly recommends immutable image tags for Kubernetes deployments.

### Validation
- Verified Maven build and test execution after provider abstraction alignment.
- Verified repository structure for Docker packaging and Kubernetes deployment assets.
- Verified GitHub Actions workflow creation and push to the feature branch.

## [0.4.0] - 2026-07-27

### Added
- Local LLM integration through Ollama.
- Support for local model enrichment using `qwen3:0.6b`.
- New Ollama client and request/response DTOs.
- New `LlmEnrichment` response block in the alert analysis API.
- Structured JSON output handling for local LLM responses.

### Changed
- Alert analysis responses now include optional LLM-based analyst enrichment.
- Human-readable summaries and recommended actions are now generated in English.
- Application configuration now includes local LLM settings in `application.yml`.

### Fixed
- Prevented noisy streamed reasoning output by using non-streaming Ollama chat calls.
- Reduced unstable free-form model output by enforcing structured response parsing.

### Security
- Sanitized logs remain in place before LLM processing.
- Local inference path avoids sending alert content to external third-party AI services.

## [0.3.0] - 2026-07-26

### Added
- Initial deterministic alert analysis API.
- Alert sanitization support.
- Incident classification, risk mapping, confidence score estimation, and recommended actions.
- Correlation IDs in analysis responses.
- Confidence score support for alert triage.

### Changed
- Improved alert analysis output structure for more traceable downstream processing.
- Refined the API response model to prepare future LLM-oriented enrichment.

### Security
- Preserved sanitization before any future model handoff.
- Continued preventing raw secrets and simple credentials from appearing in output.

### Validation
- Verified local Spring Boot startup on port 8080.
- Verified valid requests return correlation IDs, sanitized logs, and confidence scores.
- Verified invalid requests still return structured validation errors.

## [0.2.0] - 2026-07-26

### Added
- Request validation for the alert analysis API.
- Sensitive-data sanitization for incoming raw logs.
- Structured validation error responses for invalid API requests.
- Human-readable incident summaries for analyzed alerts.
- Initial incident discrimination logic for runtime execution, privilege escalation, sensitive data exposure, and endpoint security events.

### Changed
- Improved the alert analysis response to include sanitized log content.
- Improved API behavior so invalid payloads return explicit field-level validation errors.

### Security
- Masked simple secrets and credential patterns such as password, token, api key, and bearer values before analysis output.
- Kept the processing model aligned with K-Guard security principles by preventing sensitive values from being exposed in returned analysis data.

### Validation
- Verified local Spring Boot startup on port 8080.
- Verified valid alert analysis requests return sanitized output.
- Verified invalid requests return HTTP 400 with explicit field validation details.

## [0.1.0] - 2026-07-26

### Added
- Initial Spring Boot bootstrap for the K-Guard AI microservice.
- First REST endpoint for alert analysis.
- DTO-based request and response handling.
- Initial incident classification and recommended action generation.
- Local development and test execution with Maven Wrapper.
