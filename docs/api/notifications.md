# Phase 9 Notifications API

Base path: `/api/v1`

All endpoints require `Authorization: Bearer <access-token>`.

## Scope

Implemented:

- In-app notifications
- Unread count
- Mark one notification read
- Mark all notifications read
- Delete notification
- Notification preferences
- Event listeners for existing domain events
- WebSocket package structure only

Not implemented:

- Email notifications
- Slack notifications
- Push notifications
- Reports
- Analytics
- AI
- Integrations

## Notification Types

```text
TASK_ASSIGNED
TASK_UPDATED
PROJECT_CREATED
SPRINT_STARTED
SPRINT_COMPLETED
DOCUMENT_UPDATED
FILE_UPLOADED
WORKSPACE_INVITATION
COMMENT_ADDED
MENTION
```

## Endpoints

`GET /api/v1/notifications`

`GET /api/v1/notifications/unread`

`PATCH /api/v1/notifications/{id}/read`

`PATCH /api/v1/notifications/read-all`

`DELETE /api/v1/notifications/{id}`

`GET /api/v1/notification-preferences`

`PUT /api/v1/notification-preferences`

```json
{
  "preferences": [
    {
      "type": "TASK_ASSIGNED",
      "inAppEnabled": true
    },
    {
      "type": "DOCUMENT_UPDATED",
      "inAppEnabled": false
    }
  ]
}
```

## Event Coverage

Existing domain events create notifications for relevant recipients, including task assignments, task updates, comments, project creation, sprint lifecycle changes, document updates, file uploads, and workspace invitations.
