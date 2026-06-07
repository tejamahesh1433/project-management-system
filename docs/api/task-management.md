# Phase 4 Task & Issue Management API

Base path: `/api/v1`

All endpoints require `Authorization: Bearer <access-token>`.

## Scope

Implemented:

- Task CRUD
- Parent task hierarchy
- Assignment
- Status changes
- Comments
- Labels
- Task-label assignment
- Attachment persistence model

Not implemented:

- Boards
- Sprints
- Documents
- Files
- Notifications
- Reports
- Analytics
- AI
- Integrations

## Task Enums

Statuses:

```text
TODO
IN_PROGRESS
BLOCKED
DONE
ARCHIVED
```

Priorities:

```text
LOW
MEDIUM
HIGH
URGENT
```

Types:

```text
EPIC
STORY
TASK
BUG
SUBTASK
```

## Permissions

| Action | PROJECT_OWNER | PROJECT_ADMIN | PROJECT_MEMBER | PROJECT_VIEWER |
| --- | --- | --- | --- | --- |
| Read tasks | Yes | Yes | Yes | Yes |
| Create/update/delete tasks | Yes | Yes | Yes | No |
| Assign tasks | Yes | Yes | Yes | No |
| Change status | Yes | Yes | Yes | No |
| Add/update comments | Yes | Yes | Yes | No |
| Create/apply labels | Yes | Yes | Yes | No |

Assignees must already be project members. Project membership still requires workspace membership from Phase 3.

## Endpoints

### Create Task

`POST /api/v1/tasks`

```json
{
  "projectId": "uuid",
  "parentTaskId": "uuid",
  "title": "Build authentication UI",
  "description": "Create auth screens",
  "priority": "HIGH",
  "type": "TASK",
  "assigneeId": "uuid",
  "dueDate": "2026-07-01"
}
```

### List Tasks

`GET /api/v1/tasks?projectId={projectId}`

### Get Task

`GET /api/v1/tasks/{taskId}`

### Update Task

`PUT /api/v1/tasks/{taskId}`

### Delete Task

`DELETE /api/v1/tasks/{taskId}`

### Assign Task

`PATCH /api/v1/tasks/{taskId}/assignee`

```json
{
  "assigneeId": "uuid"
}
```

### Change Status

`PATCH /api/v1/tasks/{taskId}/status`

```json
{
  "status": "IN_PROGRESS"
}
```

### Create Comment

`POST /api/v1/tasks/{taskId}/comments`

```json
{
  "body": "Initial implementation is ready."
}
```

### List Comments

`GET /api/v1/tasks/{taskId}/comments`

### Update Comment

`PUT /api/v1/tasks/{taskId}/comments/{commentId}`

### Create Label

`POST /api/v1/labels`

```json
{
  "projectId": "uuid",
  "name": "backend",
  "color": "#2563eb"
}
```

### List Labels

`GET /api/v1/labels?projectId={projectId}`

### Add Label To Task

`POST /api/v1/tasks/{taskId}/labels`

```json
{
  "labelId": "uuid"
}
```

### Remove Label From Task

`DELETE /api/v1/tasks/{taskId}/labels/{labelId}`

## Events

- `TaskCreatedEvent`
- `TaskUpdatedEvent`
- `TaskDeletedEvent`
- `TaskAssignedEvent`
- `TaskStatusChangedEvent`
- `TaskCommentCreatedEvent`
- `TaskLabelAddedEvent`
