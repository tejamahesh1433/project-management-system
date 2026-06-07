#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN="${TOKEN:?TOKEN is required}"
PROJECT_ID="${PROJECT_ID:?PROJECT_ID is required}"
REQUESTS="${REQUESTS:-50}"

for i in $(seq 1 "$REQUESTS"); do
  curl -sS -o /dev/null -w "task_list_%{http_code}_%{time_total}\n" \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE_URL/api/v1/tasks?projectId=$PROJECT_ID"
done
