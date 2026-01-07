#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

BACKEND_IMAGE="sla-ping-monitor-backend:local"
FRONTEND_IMAGE="sla-ping-monitor-frontend:local"
API_BASE_URL="${NEXT_PUBLIC_API_BASE_URL:-http://localhost:8080}"
POSTGRES_IMAGE="${POSTGRES_IMAGE:-postgres:16}"

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker is not installed or not in PATH. Install Docker Desktop and ensure 'docker' is available." >&2
  exit 1
fi

if ! command -v kubectl >/dev/null 2>&1; then
  echo "Error: kubectl is not installed or not in PATH. Install kubectl and try again." >&2
  exit 1
fi

cd "$ROOT_DIR"

echo "Pulling database image: $POSTGRES_IMAGE"
docker pull "$POSTGRES_IMAGE"

echo "Building backend image: $BACKEND_IMAGE"
docker build -f deploy/backend/Dockerfile -t "$BACKEND_IMAGE" .

echo "Building frontend image: $FRONTEND_IMAGE (NEXT_PUBLIC_API_BASE_URL=$API_BASE_URL)"
docker build -f deploy/frontend/Dockerfile \
  --build-arg NEXT_PUBLIC_API_BASE_URL="$API_BASE_URL" \
  -t "$FRONTEND_IMAGE" .

echo "Deploying to Kubernetes"
kubectl delete namespace sla-ping-monitor --ignore-not-found
kubectl apply -k deploy/k8s
