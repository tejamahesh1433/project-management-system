# Analytics & Dashboards API

Phase 11 adds dashboard/widget management and current-state analytics. It does not include AI insights, forecasting, recommendations, or integrations.

## Dashboard CRUD

### Create Dashboard

`POST /api/v1/dashboards`

```json
{
  "workspaceId": "uuid",
  "projectId": "uuid",
  "name": "Delivery Dashboard"
}
```

`projectId` is optional. The authenticated user must belong to the workspace. If `projectId` is present, the user must also belong to the project.

### List Dashboards

`GET /api/v1/dashboards?workspaceId={workspaceId}`

### Get Dashboard

`GET /api/v1/dashboards/{dashboardId}`

### Update Dashboard

`PUT /api/v1/dashboards/{dashboardId}`

```json
{
  "name": "Updated Dashboard"
}
```

### Delete Dashboard

`DELETE /api/v1/dashboards/{dashboardId}`

## Widget CRUD

### Create Widget

`POST /api/v1/dashboards/{dashboardId}/widgets`

```json
{
  "type": "TASK_STATUS_CHART",
  "title": "Task Status",
  "position": 1,
  "configJson": "{\"size\":\"wide\"}"
}
```

Widget types:

- `TASK_STATUS_CHART`
- `SPRINT_PROGRESS`
- `TEAM_PERFORMANCE`
- `PROJECT_HEALTH`
- `ACTIVITY_OVERVIEW`
- `WORKSPACE_SUMMARY`

### Update Widget

`PUT /api/v1/dashboards/{dashboardId}/widgets/{widgetId}`

### Delete Widget

`DELETE /api/v1/dashboards/{dashboardId}/widgets/{widgetId}`

## Analytics

### Workspace Analytics

`GET /api/v1/analytics/workspaces/{workspaceId}`

Metrics:

- `projects`
- `tasks`
- `documents`
- `files`
- `members`
- `activities`

### Project Analytics

`GET /api/v1/analytics/projects/{projectId}`

Metrics:

- `totalTasks`
- `taskDistribution`
- `statusBreakdown`
- `sprintProgress`

### Sprint Analytics

`GET /api/v1/analytics/sprints/{sprintId}`

Metrics:

- `velocity`
- `completionPercentage`
- `storyPointsCompleted`
- `storyPointsRemaining`

### Team Analytics

`GET /api/v1/analytics/teams/projects/{projectId}`

Metrics:

- `assignedTasks`
- `completedTasks`
- `openTasks`
- `averageCompletionTimeHours`
- `overdueTasks`

## Events

- `DashboardCreatedEvent`
- `DashboardUpdatedEvent`
- `WidgetCreatedEvent`
- `AnalyticsViewedEvent`
