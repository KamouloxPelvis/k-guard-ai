# Ollama LLM for K-Guard AI

This directory contains Kubernetes manifests to deploy Ollama as the LLM backend for K-Guard AI.

## Prerequisites

- Kubernetes cluster with persistent storage support
- `kubectl` configured with cluster access
- `k-guard` namespace created

## Deployment

```bash
cd k8s/ollama
./install-ollama.sh
```

Or manually:

```bash
kubectl apply -f pvc.yaml -n k-guard
kubectl apply -f configmap.yaml -n k-guard
kubectl apply -f deployment.yaml -n k-guard
kubectl apply -f service.yaml -n k-guard
```

## Verify

```bash
kubectl -n k-guard get pods -l app=ollama
kubectl -n k-guard logs -l app=ollama
```

## Test

```bash
kubectl -n k-guard run curl-test --image=curlimages/curl -it --rm --restart=Never -- \
  curl -s http://ollama-service.k-guard.svc.cluster.local:11434/api/tags
```

## Configuration

Edit `configmap.yaml` to change the model or other settings.

## Uninstall

```bash
kubectl delete -f service.yaml -n k-guard
kubectl delete -f deployment.yaml -n k-guard
kubectl delete -f configmap.yaml -n k-guard
kubectl delete -f pvc.yaml -n k-guard
```