# Production Hardening

Phase 12 adds self-hosted hardening for Ubuntu, Docker Compose, PostgreSQL, Redis, Prometheus, Grafana, Loki, and Cloudflare Tunnel deployments.

## Security

- API rate limiting is enforced by `RateLimitingFilter`.
- Auth endpoints use a stricter rate bucket.
- Login brute force protection locks an email key after repeated failures.
- Spring Security sends hardened headers including CSP, HSTS, frame denial, and XSS protection.
- CORS is restricted by `CORS_ALLOWED_ORIGINS`.
- Basic request sanitization rejects control characters and script tags in query/header inputs.
- Passwords must be 12-128 characters and include uppercase, lowercase, number, and special character.

## Required Production Secrets

Set these through Docker Compose environment variables:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `REDIS_HOST`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`

`JWT_SECRET` must not use the default and must be at least 48 characters. Startup validation fails in `prod` or `production` profiles if required values are unsafe.

## Backups

Configure:

- `BACKUP_DIRECTORY`
- `PG_DUMP_COMMAND`
- `PG_RESTORE_COMMAND`
- `BACKUP_SCHEDULE_CRON`
- `BACKUP_SYSTEM_USER_EMAIL`

Example dump command:

```bash
pg_dump "$DATABASE_URL" --format=custom --no-owner
```

Endpoints:

- `POST /api/v1/backups`
- `GET /api/v1/backups`
- `GET /api/v1/backups/{id}/download`
- `POST /api/v1/backups/{id}/restore`

If `PG_DUMP_COMMAND` is not configured, manual backups create metadata and a placeholder file so operators can validate API and storage wiring before enabling real dumps.

Scheduled backups run only when `BACKUP_SYSTEM_USER_EMAIL` matches an existing user.

## Health

Endpoints:

- `GET /api/v1/health`
- `GET /api/v1/health/ready`

Health checks cover PostgreSQL and Redis.
