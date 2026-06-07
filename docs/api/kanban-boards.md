# Phase 5 Kanban Boards API

Base path: `/api/v1/boards`

All endpoints require `Authorization: Bearer <access-token>`.

## Scope

Implemented:

- Board CRUD
- Multiple boards per project
- Board templates: `SCRUM`, `KANBAN`
- Column CRUD
- Task movement between columns
- Task ordering within columns

Not implemented:

- Sprints
- Reports
- Analytics
- Notifications
- AI
- Documents
- Files

## Permissions

| Action | PROJECT_OWNER | PROJECT_ADMIN | PROJECT_MEMBER | PROJECT_VIEWER |
| --- | --- | --- | --- | --- |
| Read boards | Yes | Yes | Yes | Yes |
| Create/update/delete boards | Yes | Yes | No | No |
| Create/update/delete columns | Yes | Yes | No | No |
| Move tasks | Yes | Yes | Yes | No |

## Endpoints

### Create Board

`POST /api/v1/boards`

```json
{
  "projectId": "uuid",
  "name": "Delivery Board",
  "template": "KANBAN"
}
```

`KANBAN` creates `To Do`, `In Progress`, and `Done` columns.

`SCRUM` creates `Backlog`, `Selected`, `In Progress`, and `Done` columns.

### List Boards

`GET /api/v1/boards?projectId={projectId}`

### Get Board

`GET /api/v1/boards/{boardId}`

### Update Board

`PUT /api/v1/boards/{boardId}`

```json
{
  "name": "Updated Board"
}
```

### Delete Board

`DELETE /api/v1/boards/{boardId}`

### Create Column

`POST /api/v1/boards/{boardId}/columns`

```json
{
  "name": "Review",
  "position": 2
}
```

### Update Column

`PUT /api/v1/boards/{boardId}/columns/{columnId}`

```json
{
  "name": "Code Review",
  "position": 2
}
```

### Delete Column

`DELETE /api/v1/boards/{boardId}/columns/{columnId}`

### Move Task

`PATCH /api/v1/boards/{boardId}/tasks/move`

```json
{
  "taskId": "uuid",
  "columnId": "uuid",
  "position": 0
}
```

The service compacts ordering after moves so task positions within a column are stable zero-based integers.

## Events

- `BoardCreatedEvent`
- `BoardUpdatedEvent`
- `BoardDeletedEvent`
- `BoardColumnCreatedEvent`
- `BoardColumnUpdatedEvent`
- `BoardColumnDeletedEvent`
- `BoardTaskMovedEvent`
