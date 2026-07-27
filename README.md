# K-Guard AI

Current release: v0.5.0

K-Guard AI is a Java 21 and Spring Boot microservice that transforms raw security alerts into structured, human-readable incident analysis for K-Guard and similar security workflows.
It is designed as a portable backend component for DevSecOps, SOC, and platform operations, with deterministic triage, optional local LLM enrichment through Ollama, optional Elasticsearch export, and Kubernetes-ready deployment assets.

## Disclaimer

K-Guard AI is a personal and experimental MVP built for portfolio, research, and learning purposes.
It is not a production-ready SOC platform and must be reviewed, tested, hardened, and validated before use in sensitive or regulated environments.

## Overview

K-Guard AI acts as an analysis layer between security event producers and downstream consumers such as dashboards, enrichment pipelines, or incident-response workflows.

Current capabilities:
- Accept raw alerts through a REST API.
- Validate required input fields and return structured HTTP 400 responses.
- Sanitize common sensitive values before processing.
- Classify the incident into an initial security category.
- Generate a deterministic English summary.
- Estimate risk level and confidence score.
- Return recommended investigation and remediation actions.
- Optionally enrich the response with a local LLM through Ollama.
- Optionally export analyzed alerts to Elasticsearch.
- Provide container packaging and portable Kubernetes manifests for deployment.

## Architecture

Current processing flow:
1. K-Guard or another security source sends a raw alert to K-Guard AI.
2. K-Guard AI validates and sanitizes the payload.
3. K-Guard AI classifies the incident and generates a deterministic summary.
4. K-Guard AI optionally forwards sanitized context to a configured LLM provider.
5. K-Guard AI optionally exports the analyzed alert to Elasticsearch.
6. The API returns a structured response containing deterministic analysis and optional LLM enrichment.

## Implemented components

### Analysis pipeline
- Request validation.
- Sensitive-value sanitization.
- Incident classification.
- Risk scoring.
- Confidence scoring.
- Deterministic summary generation.
- Recommended action generation.

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

### Analyze alert
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

Example response:

```json
{
  "correlationId": "88ba9bdf-4970-4e62-b5df-e60e9895c165",
  "source": "falco",
  "severity": "high",
  "incidentType": "runtime-execution",
  "humanSummary": "Interactive shell execution was detected from source falco. The event \"Interactive shell detected\" suggests potentially dangerous activity inside a container. The estimated risk level is high.",
  "riskLevel": "high",
  "sanitizedLog": "Falco detected bash execution inside container in k3s workload",
  "confidenceScore": 0.95,
  "recommendedActions": [
    "Isolate the affected pod or workload.",
    "Review related Falco events and Kubernetes logs.",
    "Confirm whether the shell activity was authorized."
  ],
  "llmEnrichment": {
    "model": "qwen3:0.6b",
    "verdict": "High risk of security breach",
    "analystSummary": "The system detected an interactive shell execution in a K3s container. This could indicate a potential security breach. Further investigation is needed to confirm the cause and mitigate the risk.",
    "investigationSteps": [
      "Verify the presence of shell commands in the K3s container logs.",
      "Analyze the log entries for any suspicious shell execution patterns.",
      "Check the K3s configuration files for any misconfigurations."
    ],
    "iocs": [],
    "hypotheses": [
      "The presence of shell commands in the K3s logs indicates potential runtime execution of malicious code.",
      "Misconfigured K3s configurations could be the source of the shell execution.",
      "The log entries suggest that the shell is being executed in a container environment."
    ]
  }
}
```

## Validation behavior

Invalid requests return HTTP 400 with structured field-level errors.

Example:

```json
{
  "status": 400,
  "error": "Validation failed",
  "fields": {
    "source": "source is required",
    "rawLog": "rawLog is required",
    "title": "title is required"
  }
}
```

## Health endpoint

K-Guard AI exposes a Spring Boot Actuator health endpoint:

```bash
curl -s http://localhost:8080/actuator/health | jq
```

Expected response:

```json
{
  "status": "UP"
}
```

## Configuration

### Application properties

Example `application.yml` snippet:

```yaml
kguard:
  ai:
    llm:
      enabled: true
      provider: ollama
      base-url: http://localhost:11434
      model: qwen3:0.6b
      timeout-seconds: 30

    elasticsearch:
      export-enabled: false
      url: http://localhost:9200
      index: kguard-ai-alerts
```

### Environment variables

The service can be configured through environment variables, especially for container and Kubernetes deployments:

```bash
export SERVER_PORT=8080
export KGUARD_AI_LLM_ENABLED=true
export KGUARD_AI_LLM_PROVIDER=ollama
export KGUARD_AI_LLM_BASE_URL=http://localhost:11434
export KGUARD_AI_LLM_MODEL=qwen3:0.6b
export KGUARD_AI_LLM_TIMEOUT_SECONDS=30
export KGUARD_AI_ELASTICSEARCH_EXPORT_ENABLED=false
export KGUARD_AI_ELASTICSEARCH_URL=http://localhost:9200
export KGUARD_AI_ELASTICSEARCH_INDEX=kguard-ai-alerts
```

If Elasticsearch export is disabled, Elasticsearch credentials are not required.

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

### Test the API

```bash
curl -s http://localhost:8080/api/v1/alerts/analyze \
  -H 'Content-Type: application/json' \
  -d '{
    "source": "falco",
    "title": "Interactive shell detected",
    "severity": "high",
    "rawLog": "Falco detected bash execution inside container in k3s workload"
  }' | jq
```

## Container packaging

The repository includes a production-oriented `Dockerfile` and `.dockerignore`.

### Build image locally

```bash
docker build -t kguard-ai:local .
```

### Run image locally

```bash
docker run --rm -p 8080:8080 \
  -e KGUARD_AI_LLM_ENABLED=true \
  -e KGUARD_AI_LLM_PROVIDER=ollama \
  -e KGUARD_AI_LLM_BASE_URL=http://host.docker.internal:11434 \
  -e KGUARD_AI_LLM_MODEL=qwen3:0.6b \
  -e KGUARD_AI_LLM_TIMEOUT_SECONDS=30 \
  -e KGUARD_AI_ELASTICSEARCH_EXPORT_ENABLED=false \
  kguard-ai:local
```

## GitHub Actions and GHCR

The repository includes a GitHub Actions workflow at `.github/workflows/docker-publish.yml`.

Current behavior:
- Builds the application image on supported branch pushes and manual dispatch.
- Publishes the image to GitHub Container Registry.
- Publishes `latest`.
- Publishes Git SHA tags for immutable Kubernetes deployments.

For Kubernetes, prefer the immutable SHA tag instead of `latest`.

## Kubernetes deployment

Portable manifests are available in `k8s/`:
- `k8s/configmap.yaml`
- `k8s/deployment.yaml`
- `k8s/service.yaml`
- `k8s/secret.example.yaml`
- `k8s/README.md`

### Apply manifests

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment.yaml
kubectl rollout status deployment/kguard-ai -n k-guard --timeout=180s
```

### Important deployment notes

- Replace `ghcr.io/kamouloxpelvis/k-guard-ai:latest` with an immutable image tag before production deployment.
- Do not apply `k8s/secret.example.yaml` as-is in production.
- Create environment-specific Kubernetes Secrets outside Git when enabling sensitive integrations.
- If Elasticsearch export remains disabled, Elasticsearch credentials are not required.
- Review resource requests, limits, probes, and namespace assumptions before production use.

## Security notes

- Sensitive values must never be committed to Git.
- Sanitization is applied before returning analysis output.
- LLM enrichment uses sanitized alert content only.
- The current MVP uses local inference to avoid sending alert data to third-party AI services.
- Portable manifests intentionally avoid embedding real secrets.
- Future releases should add stronger prompt-injection defenses, provider policy controls, output validation, and richer auditability.

See also: [SECURITY.md](SECURITY.md)

## Roadmap

- v0.4.0: local LLM integration with Ollama
- v0.5.0: provider abstraction, optional Elasticsearch export, Docker packaging, GHCR publishing, and portable Kubernetes deployment assets
- v0.6.0: LLM guardrails, output policy validation, and stronger observability

## License

This project is licensed under the Apache License 2.0.
See the [LICENSE](LICENSE) file for details.

Copyright (c) 2026 Kamal Guidadou.

## Contact

- GitHub: [https://github.com/KamouloxPelvis](https://github.com/KamouloxPelvis)
- Portfolio: [https://portfolio.devopsnotes.org](https://portfolio.devopsnotes.org)
- Technical blog: [https://blog.devopsnotes.org](https://blog.devopsnotes.org)

## Roadmap

- v0.4.0: local LLM integration with Ollama
- v0.5.0: provider abstraction, optional Elasticsearch export, Docker packaging, GHCR publishing, and portable Kubernetes deployment assets
- v0.6.0: LLM guardrails, output policy validation, and stronger observability

## License

This project is licensed under the Apache License 2.0.
See the [LICENSE](LICENSE) file for details.

Copyright (c) 2026 Kamal Guidadou.

## Contact

- GitHub: [https://github.com/KamouloxPelvis](https://github.com/KamouloxPelvis)
- Portfolio: [https://portfolio.devopsnotes.org](https://portfolio.devopsnotes.org)
- Technical blog: [https://blog.devopsnotes.org](https://blog.devopsnotes.org)
