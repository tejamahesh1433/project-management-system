#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
REQUESTS="${REQUESTS:-25}"

for i in $(seq 1 "$REQUESTS"); do
  curl -sS -o /dev/null -w "auth_register_%{http_code}_%{time_total}\n" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"load-auth-$i@example.com\",\"password\":\"Password123!\",\"displayName\":\"Load User $i\"}" \
    "$BASE_URL/api/v1/auth/register"
done
