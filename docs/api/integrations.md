# Integrations API

Phase 13 adds self-hosted integration records, connection metadata, and webhook receivers. It does not add AI, mobile apps, enterprise SSO, paid SaaS integrations, or outbound paid API calls.

## Integration Types

- `GITHUB`
- `GITLAB`
- `GITEA`
- `JENKINS`
- `DOCKER`
- `KUBERNETES`
- `DISCORD`
- `TELEGRAM`
- `SMTP`

## Create Integration

`POST /api/v1/integrations`

```json
{
  "workspaceId": "uuid",
  "projectId": "uuid",
  "type": "GITHUB",
  "name": "GitHub Repo",
  "endpointUrl": "https://git.example.local/webhook",
  "repositoryUrl": "https://git.example.local/acme/project",
  "repositoryName": "acme/project",
  "metadataJson": "{\"defaultBranch\":\"main\"}"
}
```

`projectId` is optional. Git integrations can store repository URL/name/metadata. CI/CD, communication, and monitoring integrations store endpoint/config metadata for self-hosted services.

## List Integrations

`GET /api/v1/integrations?workspaceId={workspaceId}`

## Get Integration

`GET /api/v1/integrations/{id}`

## Delete Integration

`DELETE /api/v1/integrations/{id}`

## Test Connection

`POST /api/v1/integrations/{id}/test`

This validates configured endpoint metadata and updates connection status. It does not call paid external APIs.

## Webhooks

Public webhook receivers:

- `POST /api/v1/webhooks/github`
- `POST /api/v1/webhooks/gitlab`
- `POST /api/v1/webhooks/jenkins`

Webhook handlers mark matching subscriptions as received and update connection status/message.

## Supported Use Cases

- Repository linking and metadata
- Git webhook receipt
- Build/deployment status metadata
- Discord webhook endpoint storage
- Telegram bot endpoint storage
- SMTP endpoint metadata
- Grafana/Prometheus link metadata through `metadataJson`
