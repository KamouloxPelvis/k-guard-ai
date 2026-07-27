# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Planned work for v0.5.0 will be tracked here.

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
