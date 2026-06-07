# Phase 6 Sprint Management API

Base path: `/api/v1/sprints`

All endpoints require `Authorization: Bearer <access-token>`.

## Scope

Implemented:

- Sprint CRUD
- Start sprint
- Complete sprint
- Cancel sprint
- Add tasks to sprint
- Remove tasks from sprint
- Sprint task listing
- Sprint metrics

Not implemented:

- Burndown charts
- Velocity charts
- Reports
- Analytics
- Notifications
- Documents
- Files
- AI
- Integrations

## Sprint Status

```text
PLANNED
ACTIVE
COMPLETED
CANCELLED
```

## Rules

- One active sprint is allowed per project.
- Tasks must belong to the same project as the sprint.
- Project members can read sprints.
- Project owners/admins can manage sprint lifecycle and sprint setup.
- Project contributors can add and remove tasks.

## Endpoints

### Create Sprint

`POST /api/v1/sprints`

```json
{
  "projectId": "uuid",
  "name": "Sprint 1",
  "goal": "Ship task management",
  "startDate": "2026-07-01",
  "endDate": "2026-07-14"
}
```

### List Sprints

`GET /api/v1/sprints?projectId={projectId}`

### Get Sprint

`GET /api/v1/sprints/{sprintId}`

### Update Sprint

`PUT /api/v1/sprints/{sprintId}`

### Delete Sprint

`DELETE /api/v1/sprints/{sprintId}`

### Start Sprint

`POST /api/v1/sprints/{sprintId}/start`

### Complete Sprint

`POST /api/v1/sprints/{sprintId}/complete`

### Cancel Sprint

`POST /api/v1/sprints/{sprintId}/cancel`

### Add Task To Sprint

`POST /api/v1/sprints/{sprintId}/tasks`

```json
{
  "taskId": "uuid"
}
```

### Remove Task From Sprint

`DELETE /api/v1/sprints/{sprintId}/tasks/{taskId}`

### List Sprint Tasks

`GET /api/v1/sprints/{sprintId}/tasks`

### Sprint Metrics

`GET /api/v1/sprints/{sprintId}/metrics`

Response:

```json
{
  "sprintId": "uuid",
  "totalTasks": 4,
  "completedTasks": 2,
  "remainingTasks": 2,
  "completionPercentage": 50.0,
  "storyPointsCompleted": 8,
  "storyPointsRemaining": 5
}
```

## Events

- `SprintCreatedEvent`
- `SprintUpdatedEvent`
- `SprintStartedEvent`
- `SprintCompletedEvent`
- `SprintCancelledEvent`
- `SprintTaskAddedEvent`
- `SprintTaskRemovedEvent`
