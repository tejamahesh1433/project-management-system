# Phase 8 Activity Feed & Audit Logs API

Base paths:

- `/api/v1/activity`
- `/api/v1/audit`

All endpoints require `Authorization: Bearer <access-token>`.

## Scope

Implemented:

- User activity timeline
- Workspace activity feed
- Project activity feed
- Audit log history
- Entity audit history
- Before/after value fields
- Actor tracking
- IP address tracking
- User agent tracking
- Event listeners for existing project, task, sprint, document, file, and board events

Not implemented:

- Email notifications
- Push notifications
- Reports
- Analytics
- AI
- Integrations

## Endpoints

### User Activity Timeline

`GET /api/v1/activity`

Returns activity rows where the authenticated user is the actor.

### Workspace Activity Feed

`GET /api/v1/activity/workspaces/{workspaceId}`

Requires workspace membership.

### Project Activity Feed

`GET /api/v1/activity/projects/{projectId}`

Requires project membership.

### User Audit Logs

`GET /api/v1/audit`

Returns audit logs where the authenticated user is the actor.

### Entity Audit History

`GET /api/v1/audit/{entityType}/{entityId}`

Example:

`GET /api/v1/audit/TASK/6f6b7c42-1f8f-4e49-8f62-3ed6f942f677`

## Event Coverage

Activity and audit rows are generated automatically from existing domain events:

- Project events
- Task events
- Sprint events
- Board events
- Document events
- File events

Because existing domain events carry compact payloads, `beforeValue` and `afterValue` store event snapshots rather than full entity diffs.
