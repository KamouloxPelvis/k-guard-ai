# K-Guard AI Kubernetes manifests

## Files
- `configmap.yaml`: non-sensitive runtime configuration
- `deployment.yaml`: application deployment
- `service.yaml`: ClusterIP service
- `secret.example.yaml`: example only, do not apply as-is in production

## Important
- Replace `ghcr.io/kamouloxpelvis/k-guard-ai:latest` with an immutable image tag.
- Prefer a Git SHA image tag for Kubernetes deployments.
- Do not commit real secrets.
- If Elasticsearch export remains disabled, no Elasticsearch credentials are required.

## Example deployment flow
```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment.yaml
kubectl rollout status deployment/kguard-ai -n k-guard --timeout=180s
```
