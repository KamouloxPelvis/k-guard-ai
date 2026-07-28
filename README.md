# K-Guard AI

Current release: v0.7.0

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
- Provide an interactive Kubernetes installer for namespace-aware deployment.

## Architecture

Current processing flow:
1. K-Guard or another security source sends a raw or normalized alert to K-Guard AI.
2. K-Guard AI validates and sanitizes the payload.
3. K-Guard AI normalizes contextual fields when using the normalized ingestion path.
4. K-Guard AI classifies the incident and generates a deterministic summary.
5. K-Guard AI optionally forwards sanitized context to a local Ollama runtime.
6. K-Guard AI optionally exports the analyzed alert to Elasticsearch.
7. The API returns a structured response containing deterministic analysis and optional local LLM enrichment.

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

### Local AI integration
- Provider abstraction layer.
- Ollama provider implementation.
- Local model support for lightweight enrichment workflows.

### Delivery and deployment
- Docker image build support.
- GitHub Actions workflow for GHCR publishing.
- Kubernetes manifests in `k8s/`.
- ConfigMap-based runtime configuration.
- Kubernetes Secret integration for Elasticsearch credentials.
- Interactive Kubernetes installer in `installer/`.
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

### Installer
- Go
- kubectl
- Kubernetes / K3s oriented deployment workflow

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
  "version": "0.7.0",
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
docker build -t kguard-ai:v0.7.0 .
```

## Kubernetes installer

K-Guard AI ships with an interactive Kubernetes installer located in `installer/`.

Current installer behavior:
- Checks that `kubectl` is available.
- Checks cluster accessibility with the current kubeconfig.
- Detects namespace `k-guard` and uses it by default when it exists.
- Otherwise asks the user to enter a target namespace.
- Prompts the user for the Elasticsearch username.
- Prompts the user for the Elasticsearch password without echoing it in the terminal.
- Creates or updates the Kubernetes Secret `kguard-ai-secret`.
- Applies the ConfigMap, Deployment, and Service manifests.
- Waits for the deployment rollout status.

### Run installer locally

```bash
cd installer
go run . check
go run . install
go run . status
```

## VPS deployment approach

For the current v0.7.0 workflow, the simplest supported deployment approach is to clone the repository on the target machine and run the required commands from the project directory.

Example:

```bash
git clone https://github.com/KamouloxPelvis/K-Guard-AI.git
cd k-guard-ai
```

From there, you can either:
- run the Java service locally or in Docker for a VPS-oriented setup;
- or use the Kubernetes installer from `installer/` for a cluster-connected environment.

### Update on target machine

For a simple update workflow on the target machine:

```bash
cd K-Guard-AI
git pull
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
  ghcr.io/kamouloxpelvis/k-guard-ai:v0.7.0
```

## Integration approach

Recommended first integration path with K-Guard:
- K-Guard sends alerts to K-Guard AI over HTTP.
- K-Guard AI returns deterministic JSON analysis.
- Optional Ollama enrichment can be enabled depending on the target environment.
- Optional Elasticsearch export can be enabled when downstream indexing is required.

This keeps the first portfolio integration simple, portable, and easy to troubleshoot.

## Repository structure

```text
.
├── installer/                 # Interactive Kubernetes installer written in Go
├── k8s/                       # Kubernetes manifests
├── src/                       # Spring Boot application source code
├── .github/workflows/         # CI workflow(s)
├── Dockerfile
├── pom.xml
└── README.md
```
