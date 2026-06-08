#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

BACKEND_PORT="${BACKEND_PORT:-8081}"
FRONTEND_PORT="${FRONTEND_PORT:-3004}"
DATABASE_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5433/pms_db}"
CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:3000,http://localhost:3001,http://localhost:3004}"

BACKEND_PID=""
FRONTEND_PID=""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

cleanup() {
  echo
  echo "Stopping development servers..."
  if [[ -n "$FRONTEND_PID" ]] && kill -0 "$FRONTEND_PID" 2>/dev/null; then
    kill "$FRONTEND_PID" 2>/dev/null || true
  fi
  if [[ -n "$BACKEND_PID" ]] && kill -0 "$BACKEND_PID" 2>/dev/null; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi
}

trap cleanup EXIT INT TERM

echo -e "${BLUE}Starting development environment...${NC}\n"

# 1. Start Docker services
echo -e "${GREEN}Starting Docker services (PostgreSQL, Redis)...${NC}"
docker compose -f "$ROOT_DIR/docker-compose.dev.yml" up -d

# Wait for database to be ready
echo -n "Waiting for database..."
for i in {1..30}; do
  if docker exec pms_postgres_dev pg_isready -U pms_user -d pms_db > /dev/null 2>&1; then
    echo -e "${GREEN} Ready!${NC}"
    break
  fi
  echo -n "."
  sleep 1
done

# 2. Start Backend
echo -e "\n${GREEN}Starting Spring Boot backend on port $BACKEND_PORT...${NC}"
(
  cd "$BACKEND_DIR"
  export SPRING_PROFILES_ACTIVE=dev
  DATABASE_URL="$DATABASE_URL" \
  DATABASE_USERNAME="pms_user" \
  DATABASE_PASSWORD="pms_pass" \
  SERVER_PORT="$BACKEND_PORT" \
  CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS" \
  ./gradlew bootRun
) &
BACKEND_PID="$!"

echo "Starting Next.js frontend on http://localhost:$FRONTEND_PORT..."
(
  cd "$FRONTEND_DIR"
  API_BASE_URL="http://127.0.0.1:$BACKEND_PORT" PORT="$FRONTEND_PORT" npm run dev
) &
FRONTEND_PID="$!"

echo
echo "Development stack is starting:"
echo "  Frontend: http://localhost:$FRONTEND_PORT"
echo "  Backend:  http://localhost:$BACKEND_PORT"
echo "  Postgres: localhost:5433"
echo "  Redis:    localhost:6379"
echo
echo "Press Ctrl+C to stop backend and frontend. Docker containers stay running."

wait "$BACKEND_PID" "$FRONTEND_PID"
