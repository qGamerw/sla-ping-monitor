# Docker Desktop Kubernetes deployment (local)

These manifests and Dockerfiles are intended for local deployment to Docker Desktop Kubernetes
without pushing images to a registry.

## Build images

From the repository root:

```bash
docker build -f deploy/backend/Dockerfile -t sla-ping-monitor-backend:local .
docker build -f deploy/frontend/Dockerfile \
  --build-arg NEXT_PUBLIC_API_BASE_URL=http://localhost:8080 \
  -t sla-ping-monitor-frontend:local .
```

## Build and deploy script

```bash
./deploy/deploy.sh
```

You can override the API base URL:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080 ./deploy/deploy.sh
```

The script pulls the database image before deploying. You can override it if needed:

```bash
POSTGRES_IMAGE=postgres:16 ./deploy/deploy.sh
```

## Deploy

```bash
kubectl apply -k deploy/k8s
```

## Access services

* Frontend: http://localhost:3000
* Backend: http://localhost:8080

## Cleanup

```bash
kubectl delete -k deploy/k8s
```
