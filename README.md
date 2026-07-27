# K-Guard AI

Current release: v0.6.0

K-Guard AI is a Java 21 and Spring Boot microservice that transforms raw or normalized security alerts into structured, human-readable incident analysis for K-Guard and similar security workflows.

It is designed as a portable backend component for DevSecOps, SOC, and platform operations, with deterministic triage, optional local LLM enrichment through Ollama, optional Elasticsearch export, profile-aware runtime configuration, and Kubernetes-ready deployment assets.

## Disclaimer

K-Guard AI is a personal and experimental MVP built for portfolio, research, and learning purposes.
It is not a production-ready SOC platform and must be reviewed, tested, hardened, and validated before use in sensitive or regulated environments.

## Overview

K-Guard AI acts as an analysis layer between security event producers and downstream consumers such as dashboards, enrichment pipelines, or incident-response workflows.

Current capabilities:
- Accept raw alerts through a REST API.
- Accept normalized alerts through a dedicated ingestion endpoint.
- Validate required input fields and return structured HTTP 400 responses.
- Sanitize common sensitive values before processing.
- Classify the incident into an initial security category.
- Generate a deterministic English summary.
- Estimate risk level and confidence score.
- Return recommended investigation and remediation actions.
- Optionally enrich the response with a local LLM through Ollama.
- Optionally export analyzed alerts to Elasticsearch.
- Expose service capabilities for runtime inspection.
- Provide container packaging and portable Kubernetes manifests for deployment.

## Architecture

Current processing flow:
1. K-Guard or another security source sends a raw or normalized alert to K-Guard AI.
2. K-Guard AI validates and sanitizes the payload.
3. K-Guard AI normalizes contextual fields when using the normalized ingestion path.
4. K-Guard AI classifies the incident and generates a deterministic summary.
5. K-Guard AI optionally forwards sanitized context to a configured LLM provider.
6. K-Guard AI optionally exports the analyzed alert to Elasticsearch.
7. The API returns a structured response containing deterministic analysis and optional LLM enrichment.

## Implemented components

### Analysis pipeline
- Request validation.
- Sensitive-value sanitization.
- Alert normalization support.
- Incident classification.
- Risk scoring.
- Confidence scoring.
- Deterministic summary generation.
- Recommended action generation.

### Runtime API
- Raw alert analysis endpoint.
- Normalized alert ingestion endpoint.
- Service capabilities endpoint.
- Spring Boot Actuator health and info endpoints.

### LLM integration
- Provider abstraction layer.
- Ollama provider implementation.
- Local model support for lightweight enrichment workflows.

### Delivery and deployment
- Docker image build support.
- GitHub Actions workflow for GHCR publishing.
- Kubernetes manifests in `k8s/`.
- ConfigMap-based runtime configuration.
- Portable secret placeholder for optional sensitive settings.
- Profile-aware configuration for local, VPS, and Kubernetes targets.

## Tech stack

### Backend
- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Validation
- Spring Actuator
- Jackson
- Maven Wrapper

### Local AI
- Ollama
- Qwen3 (`qwen3:0.6b`) for the current lightweight local MVP path

### Target ecosystem
- Kubernetes
- K3s
- Falco
- Wazuh
- Elasticsearch
- Kibana
- Ollama

## API

### Analyze raw alert
`POST /api/v1/alerts/analyze`

Example request:

```json
{
  "source": "falco",
  "title": "Interactive shell detected",
  "severity": "high",
  "rawLog": "Falco detected bash execution inside container in k3s workload"
}
```

### Ingest normalized alert
`POST /api/v1/ingest/normalized`

Example request:

```json
{
  "source": "falco",
  "event": {
    "title": "Terminal shell in container",
    "severity": "critical",
    "rawLog": "Falco detected bash execution inside container",
    "eventId": "falco-001",
    "host": "k3s-node-1",
    "workload": "payments-api",
    "category": "runtime"
  },
  "metadata": {
    "cluster": "k3s-dev",
    "namespace": "production"
  }
}
```

### Service capabilities
`GET /api/v1/service/capabilities`

Example response:

```json
{
  "service": "k-guard-ai",
  "version": "0.6.0",
  "defaultLanguage": "en",
  "maxRawLogLength": 4000,
  "includeSanitizedLogInResponse": true,
  "llmEnabled": false,
  "llmProvider": "ollama",
  "elasticsearchExportEnabled": false,
  "supportedSources": [
    "falco",
    "wazuh",
    "kguard",
    "generic",
    "fluent-bit",
    "elasticsearch"
  ],
  "supportedProfiles": [
    "local",
    "vps",
    "kubernetes"
  ],
  "activeFeatures": [
    "deterministic-analysis",
    "sanitization",
    "risk-scoring",
    "recommended-actions",
    "normalized-ingestion"
  ]
}
```

## Health endpoints

K-Guard AI exposes Spring Boot Actuator endpoints for runtime checks:

```bash
curl -s http://localhost:8080/actuator/health | jq
curl -s http://localhost:8080/actuator/health/liveness | jq
curl -s http://localhost:8080/actuator/health/readiness | jq
curl -s http://localhost:8080/actuator/info | jq
```

## Configuration

Main application settings are defined in:

- `src/main/resources/application.yml`

Current configuration includes:
- `kguard.ai.*`
- `kguard.ai.llm.*`
- `kguard.ai.elasticsearch.*`

Profiles included:
- `local`
- `vps`
- `kubernetes`

## Local development

### Prerequisites
- Java 21
- Maven Wrapper
- Ollama installed locally
- Model available locally, for example:

```bash
ollama pull qwen3:0.6b
```

### Build and test

```bash
./mvnw clean test
```

### Run locally

```bash
./mvnw spring-boot:run
```

### Build container image

```bash
docker build -t kguard-ai:v0.6.0 .
```

## VPS runtime example

Example minimal VPS startup with deterministic analysis only:

```bash
docker run -d --name kguard-ai \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=vps \
  -e KGUARD_AI_LLM_ENABLED=false \
  -e KGUARD_AI_ELASTICSEARCH_EXPORT_ENABLED=false \
  ghcr.io/kamouloxpelvis/k-guard-ai:v0.6.0
```

## Integration approach

Recommended first integration path with K-Guard:
- K-Guard sends alerts to K-Guard AI over HTTP.
- K-Guard AI returns deterministic JSON analysis.
- Optional LLM enrichment remains disabled for the initial VPS deployment.
- Optional Elasticsearch export can be enabled later if needed.

This keeps the first portfolio integration simple, portable, and easy to troubleshoot.
