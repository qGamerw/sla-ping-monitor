#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

BACKEND_IMAGE="sla-ping-monitor-backend:local"
FRONTEND_IMAGE="sla-ping-monitor-frontend:local"
API_BASE_URL="${NEXT_PUBLIC_API_BASE_URL:-http://localhost:8080}"

cd "$ROOT_DIR"

echo "Building backend image: $BACKEND_IMAGE"
docker build -f deploy/backend/Dockerfile -t "$BACKEND_IMAGE" .

echo "Building frontend image: $FRONTEND_IMAGE (NEXT_PUBLIC_API_BASE_URL=$API_BASE_URL)"
docker build -f deploy/frontend/Dockerfile \
  --build-arg NEXT_PUBLIC_API_BASE_URL="$API_BASE_URL" \
  -t "$FRONTEND_IMAGE" .

echo "Deploying to Kubernetes"
kubectl apply -k deploy/k8s
