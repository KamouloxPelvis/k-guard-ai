# K-Guard AI

K-Guard AI is a Falco alert enrichment microservice built with Spring Boot and Java. It interacts with local LLMs (Ollama) and can export enriched alerts to Elasticsearch.

This repository contains:

- the K-Guard AI service (Spring Boot, Java 21)
- Kubernetes manifests (`k8s/`) to deploy it into a k3s/Kubernetes cluster
- a Go installer (`installer/`) with both TUI and CLI interfaces to automate and standardize installation

---

## Prerequisites

- Java 21 for local development (Maven, tests, etc.)
- Go 1.22+ to build the installer
- A k3s/Kubernetes cluster accessible via `kubectl` (for in-cluster installation)
- Optional: a Linux VPS for off-cluster deployment via systemd + Nginx

---

## Building the service locally

```bash
git clone https://github.com/KamouloxPelvis/K-Guard-AI.git
cd K-Guard-AI

mvn clean test
mvn clean package
```

The JAR is generated in `target/` (for example `target/k-guard-ai-0.8.0.jar`).

---

## Installation in a k3s / Kubernetes cluster (v0.8.0)

### 1. Build the Go installer

```bash
cd K-Guard-AI

go -C installer mod tidy
go -C installer build -o ./installer/kguard-ai-installer .
```

### 2. Check cluster connectivity

```bash
cd installer

./kguard-ai-installer check
```

This command:

- verifies that `kubectl` is present
- checks cluster access
- optionally detects an existing K-Guard namespace

### 3. Install K-Guard AI via TUI

From `installer/`:

```bash
./kguard-ai-installer
```

TUI interface:

- use ↑/↓ or j/k to navigate
- `enter` to trigger an action (`check`, `install`, `status`)
- `q` or `Ctrl+C` to quit

The installation flow (`Install K-Guard AI`) will:

1. Verify `kubectl` and cluster access
2. Resolve the namespace:
   - if an existing K-Guard namespace is detected (e.g. `k-guard`), it will be proposed
   - otherwise, a namespace is requested in CLI (`Enter target namespace for K-Guard AI:`)
3. Prompt for Elasticsearch credentials (username + password)
4. Create or update the Elasticsearch Secret in the target namespace
5. Apply the manifests:
   - `k8s/configmap.yaml`
   - `k8s/deployment.yaml`
   - `k8s/service.yaml`
6. Wait for the rollout of the `kguard-ai` Deployment

### 4. CLI mode (scripts, CI/CD)

The same operations can be triggered in CLI mode:

```bash
cd installer

./kguard-ai-installer check
./kguard-ai-installer install
./kguard-ai-installer status
```

---

## Kubernetes manifests (v0.8.0)

The files in `k8s/` describe:

- `k8s/deployment.yaml`: K-Guard AI deployment (`Deployment kguard-ai`)
- `k8s/service.yaml`: internal service (`Service kguard-ai`, port 8080)
- `k8s/configmap.yaml`: LLM and export configuration
- `k8s/secret.example.yaml`: example Secret for Elasticsearch credentials

By default, the ConfigMap:

- enables LLM enrichment
- disables Elasticsearch export (`KGUARD_AI_ELASTICSEARCH_EXPORT_ENABLED: "false"`)

You can enable export by setting this value to `"true"` once your credentials and Elasticsearch URL are correctly configured.

---

## Off-cluster deployment (VPS + systemd + Nginx)

As an alternative to in-cluster deployment, K-Guard AI can be deployed on a Linux VPS:

1. Copy the generated JAR (`target/k-guard-ai-0.8.0.jar`) to the VPS (`/opt/kguard-ai/app.jar`)
2. Create a systemd service (`/etc/systemd/system/kguard-ai.service`) to run the JAR with Java 21
3. Install Nginx and configure a reverse proxy:
   - expose the K-Guard AI HTTP API (port 8080)
   - handle TLS/HTTPS via certbot if needed
4. Configure Falco (or K-Guard) to call the K-Guard AI enrichment API on the VPS

This approach is useful to demonstrate a complete DevOps flow:  
Maven build → systemd service → Nginx reverse proxy → enrichment API accessible from the cluster or external agents.

---

## Security and hardening

K-Guard AI v0.8.0 includes:

- a hardened Kubernetes deployment (`runAsNonRoot`, `readOnlyRootFilesystem`, `drop ALL capabilities`, `seccompProfile RuntimeDefault`)
- controlled LLM configuration (timeouts, model, base URL)
- optional integration with Elasticsearch (via Secret and ConfigMap)

These choices are documented in `CHANGELOG.md` and `SECURITY.md`.

