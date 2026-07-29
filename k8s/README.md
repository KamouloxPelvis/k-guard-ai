# Kubernetes deployment

This directory contains the Kubernetes manifests for K-Guard AI.

## Included manifests

- `configmap.yaml`
- `deployment.yaml`
- `service.yaml`
- `secret.example.yaml`

## Security baseline

The deployment manifest applies a first security-hardening baseline:

- Pod-level non-root execution
- Explicit runtime identity with `runAsUser`, `runAsGroup`, and `fsGroup`
- `seccompProfile` set to `RuntimeDefault`
- Container privilege escalation disabled
- All Linux capabilities dropped
- Read-only root filesystem enabled
- Writable `/tmp` provided through an `emptyDir` volume

This approach keeps the container filesystem immutable at runtime while still allowing temporary writes required by the Java application.

## Deployment

Apply the manifests with:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

If Elasticsearch export is enabled, create the Kubernetes Secret from your real credentials before applying the deployment manifest.

## Notes

- Do not commit real secrets to Git.
- Prefer immutable image tags for production-style deployments.
- Review resource limits, namespace, and image tag before applying in a target cluster.
