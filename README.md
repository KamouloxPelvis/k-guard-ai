# K-Guard AI

Current release: v0.4.0

K-Guard AI is an independent Java microservice that transforms raw security alerts into structured, human-readable incident analysis for K-Guard.
It is designed to support DevSecOps, SOC, and platform operations with deterministic alert triage and optional local LLM enrichment.

## Disclaimer

K-Guard AI is a personal and experimental MVP built for portfolio, research, and learning purposes.
It is not a production-ready SOC platform and must be reviewed, tested, and hardened before use in sensitive environments.

## Overview

K-Guard AI acts as an analysis layer between security event sources and downstream remediation, dashboarding, or AI-assisted workflows.

Current capabilities:
- Accept raw alerts through a REST API.
- Validate required input fields.
- Sanitize common sensitive values before processing.
- Classify the incident into an initial security category.
- Generate a deterministic English summary.
- Estimate risk level and confidence score.
- Return recommended investigation and remediation actions.
- Optionally enrich the response with a local LLM through Ollama.

## Architecture

Current processing flow:
1. K-Guard or another security source sends a raw alert to K-Guard AI.
2. K-Guard AI validates and sanitizes the payload.
3. K-Guard AI classifies the incident and generates a deterministic summary.
4. K-Guard AI optionally sends the sanitized context to a local Ollama model.
5. The API returns a structured response with deterministic analysis and optional LLM enrichment.
6. Future releases may export enriched output to Elasticsearch and Kubernetes-native consumers.

## Tech Stack

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
- Qwen3 (`qwen3:0.6b`) for the current local MVP path

### Target ecosystem
- Kubernetes
- K3s
- Falco
- Wazuh
- Elasticsearch
- Kibana
- vLLM (planned)

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

K-Guard AI exposes a basic Spring Boot Actuator health endpoint:

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
```

## Security notes

- Sensitive values must never be committed to Git.
- Sanitization is applied before returning analysis output.
- LLM enrichment uses sanitized alert content only.
- The current MVP uses local inference to avoid sending alert data to third-party AI services.
- Future releases should add stronger prompt-injection defenses, allowlists, output validation, and traceability.

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

## Roadmap

- v0.4.0: local LLM integration with Ollama
- v0.5.0: provider abstraction, vLLM support, Elasticsearch export, and Kubernetes deployment assets
- v0.6.0: LLM guardrails, output policy validation, and stronger observability

## License

This project is licensed under the Apache License 2.0.
See the [LICENSE](LICENSE) file for details.

Copyright (c) 2026 Kamal Guidadou.

## Contact

- GitHub: [https://github.com/KamouloxPelvis](https://github.com/KamouloxPelvis)
- Portfolio: [https://portfolio.devopsnotes.org](https://portfolio.devopsnotes.org)
- Technical blog: [https://blog.devopsnotes.org](https://blog.devopsnotes.org)
