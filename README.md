# K-Guard AI

Current release: v0.2.0

K-Guard AI is an independent Java microservice designed to transform raw security alerts into human-readable incident summaries for K-Guard. It is intended to support DevSecOps, SOC, and platform operations by classifying alerts, sanitizing sensitive content, and preparing enriched analysis that can later be consumed by dashboards, Elasticsearch, or a local LLM pipeline.

## Disclaimer

K-Guard AI is a personal and experimental MVP built for portfolio, research, and learning purposes. It is not a production-ready SOC platform and must be reviewed, tested, and hardened before use in sensitive environments.

## Overview

K-Guard AI acts as an intelligent analysis layer between K-Guard event sources and future AI-assisted workflows.

Current MVP capabilities:
- Accept a raw alert through a REST API.
- Validate required fields.
- Sanitize common sensitive values before analysis output.
- Classify the incident into an initial security category.
- Return a human-readable summary in French.
- Provide an initial risk level and remediation guidance.

## Architecture

Planned processing flow:
1. K-Guard or another security source sends a raw alert to K-Guard AI.
2. K-Guard AI validates and sanitizes the payload.
3. K-Guard AI classifies the incident and generates a readable summary.
4. A future release will forward sanitized content to a local LLM running in Kubernetes.
5. Enriched output can later be indexed into Elasticsearch or shown in K-Guard dashboards.

## Tech Stack

### Backend
- Java 25
- Spring Boot 3
- Spring Web
- Spring Validation
- Spring Actuator
- Maven Wrapper

### Target ecosystem
- Kubernetes
- K3s
- Falco
- Wazuh
- Elasticsearch
- Kibana
- Ollama or vLLM (future release)

## API

### Analyze alert
`POST /api/v1/alerts/analyze`

Example request:
```json
{
  "source": "falco",
  "title": "Interactive shell detected",
  "severity": "critical",
  "rawLog": "A shell bash was spawned inside a container by root user with token=abcd1234 and password=supersecret"
}
```

Example response:
```json
{
  "source": "falco",
  "severity": "critical",
  "incidentType": "runtime-execution",
  "humanSummary": "Une exécution de shell interactive a été détectée depuis la source falco. L'événement \"Interactive shell detected\" suggère une activité potentiellement dangereuse dans un conteneur. Le niveau de risque estimé est high.",
  "riskLevel": "high",
  "sanitizedLog": "A shell bash was spawned inside a container by root user with token=[REDACTED] and password=[REDACTED]",
  "recommendedActions": [
    "Isoler le pod ou la charge de travail concernée.",
    "Vérifier les événements Falco et les logs Kubernetes associés.",
    "Confirmer si l'ouverture du shell était autorisée ou non."
  ]
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

## Security notes

- Sensitive values must never be committed to Git.
- Sanitization is applied before returning analysis output.
- This service is designed to support a future sovereign LLMOps model based on local LLM inference.
- Future releases should add stronger prompt-injection defenses, allowlists, output validation, and traceability.

## Local development

### Prerequisites
- Java 25
- Maven Wrapper

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
curl -X POST http://localhost:8080/api/v1/alerts/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "source": "falco",
    "title": "Interactive shell detected",
    "severity": "critical",
    "rawLog": "A shell bash was spawned inside a container by root user with token=abcd1234 and password=supersecret"
  }'
```

## Roadmap

- v0.3.0: correlation ID, confidence score, LLM-ready response model
- v0.4.0: local LLM integration with Ollama or vLLM
- v0.5.0: Elasticsearch export and Kubernetes deployment manifests
- v0.6.0: LLM guardrails and output policy validation

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

Copyright (c) 2026 Kamal Guidadou.

## Contact & Credits

Copyright © 2026 Kamal Guidadou. Licensed Apache 2.0 License.

Git Repo : https://github.com/KamouloxPelvis/
Portfolio: https://portfolio.devopsnotes.org  
Technical blog: https://blog.devopsnotes.org
