# RC Load Test Report

## Created Scripts

- `load-testing/authentication-load-test.sh`
- `load-testing/task-load-test.sh`
- `load-testing/board-load-test.sh`
- `load-testing/analytics-load-test.sh`

## RC Result

Scripts are lightweight `curl`-based smoke load tests for a self-hosted server with no paid tooling.

## How To Run

```bash
BASE_URL=http://localhost:8080 TOKEN=<jwt> WORKSPACE_ID=<uuid> PROJECT_ID=<uuid> BOARD_ID=<uuid> ./load-testing/task-load-test.sh
```

## Recommendation

Run against staging before RC signoff and capture p95 latency from reverse-proxy logs.
