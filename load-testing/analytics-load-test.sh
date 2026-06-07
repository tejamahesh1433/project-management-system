#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN="${TOKEN:?TOKEN is required}"
WORKSPACE_ID="${WORKSPACE_ID:?WORKSPACE_ID is required}"
PROJECT_ID="${PROJECT_ID:?PROJECT_ID is required}"
REQUESTS="${REQUESTS:-30}"

for i in $(seq 1 "$REQUESTS"); do
  curl -sS -o /dev/null -w "analytics_workspace_%{http_code}_%{time_total}\n" \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE_URL/api/v1/analytics/workspaces/$WORKSPACE_ID"
  curl -sS -o /dev/null -w "analytics_project_%{http_code}_%{time_total}\n" \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE_URL/api/v1/analytics/projects/$PROJECT_ID"
done
