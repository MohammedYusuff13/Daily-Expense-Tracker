#!/usr/bin/env bash
# Starts both servers for a Codespace / Gitpod / local terminal.
# Backend runs in the background; frontend runs in the foreground.
set -e

echo "==> Starting Spring Boot backend on :8080 (logs -> backend.log)"
( cd backend && mvn -q spring-boot:run > ../backend.log 2>&1 ) &
BACK_PID=$!

# Stop the backend when this script is interrupted (Ctrl+C).
trap "echo; echo '==> Stopping backend'; kill $BACK_PID 2>/dev/null || true" EXIT

echo "==> Waiting for the backend to answer on :8080 ..."
for i in $(seq 1 60); do
  if curl -sf http://localhost:8080/api/dashboard > /dev/null 2>&1; then
    echo "==> Backend is up."
    break
  fi
  sleep 2
done

echo "==> Starting React frontend on :3000"
cd frontend
npm install
npm run dev
