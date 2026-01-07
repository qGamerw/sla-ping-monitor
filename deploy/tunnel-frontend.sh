#!/usr/bin/env bash
set -euo pipefail

FRONTEND_PORT="${FRONTEND_PORT:-3000}"

if ! command -v ngrok >/dev/null 2>&1; then
  echo "Error: ngrok is not installed or not in PATH. Install ngrok and try again." >&2
  exit 1
fi

echo "Starting ngrok tunnel for frontend on port ${FRONTEND_PORT}"
ngrok http "${FRONTEND_PORT}"
