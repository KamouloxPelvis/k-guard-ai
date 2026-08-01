#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${NAMESPACE:-k-guard}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "🚀 Deploying Ollama in namespace ${NAMESPACE}..."

# Apply PVC
echo "Creating PersistentVolumeClaim..."
kubectl apply -f "${SCRIPT_DIR}/pvc.yaml" -n "${NAMESPACE}"

# Apply ConfigMap
echo "Creating ConfigMap..."
kubectl apply -f "${SCRIPT_DIR}/configmap.yaml" -n "${NAMESPACE}"

# Apply Deployment
echo "Creating Deployment..."
kubectl apply -f "${SCRIPT_DIR}/deployment.yaml" -n "${NAMESPACE}"

# Apply Service
echo "Creating Service..."
kubectl apply -f "${SCRIPT_DIR}/service.yaml" -n "${NAMESPACE}"

# Wait for pod to be ready
echo "⏳ Waiting for Ollama pod to be ready..."
kubectl -n "${NAMESPACE}" wait --for=condition=Ready pod -l app=ollama --timeout=300s

# Pull model
echo "📦 Pulling model qwen3:0.6b..."
kubectl -n "${NAMESPACE}" exec -i deploy/ollama -- ollama pull qwen3:0.6b

echo "✅ Ollama deployment completed successfully!"
echo ""
echo "To test Ollama:"
echo "  kubectl -n ${NAMESPACE} run curl-test --image=curlimages/curl -it --rm --restart=Never -- curl -s http://ollama-service.${NAMESPACE}.svc.cluster.local:11434/api/tags"