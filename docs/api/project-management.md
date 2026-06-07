# Phase 3 Project Management API

Base path: `/api/v1/projects`

All endpoints require `Authorization: Bearer <access-token>`.

## Scope

Implemented:

- Project CRUD
- Archive and restore
- Soft delete
- Project membership
- Project role management
- Project permission enforcement

Not implemented in Phase 3:

- Tasks
- Boards
- Sprints
- Documents
- Files
- Notifications
- Activity feed
- Reports
- Analytics
- AI
- Integrations

## Project Status

```text
ACTIVE
ARCHIVED
COMPLETED
```

## Project Roles

```text
PROJECT_OWNER
PROJECT_ADMIN
PROJECT_MEMBER
PROJECT_VIEWER
```

Project membership is separate from workspace membership. A user must already belong to the workspace before they can be added to a project.

## Permission Matrix

| Action | Workspace OWNER/ADMIN | PROJECT_OWNER | PROJECT_ADMIN | PROJECT_MEMBER | PROJECT_VIEWER |
| --- | --- | --- | --- | --- | --- |
| Create project | Yes | N/A | N/A | N/A | N/A |
| Read project | If project member | Yes | Yes | Yes | Yes |
| Update project | If project member and project admin/owner | Yes | Yes | No | No |
| Archive/restore | If project member and project admin/owner | Yes | Yes | No | No |
| Delete project | If project owner | Yes | No | No | No |
| Add member | If project admin/owner | Yes | Yes, except owner | No | No |
| Change member role | If project admin/owner | Yes | Yes, except owner | No | No |
| Remove member | If project admin/owner | Yes | Yes, except owner | No | No |

## Endpoints

### Create Project

`POST /api/v1/projects`

```json
{
  "workspaceId": "uuid",
  "name": "Roadmap",
  "slug": "roadmap",
  "description": "Product roadmap project",
  "color": "#2563eb",
  "icon": "map"
}
```

### List Projects

`GET /api/v1/projects?workspaceId={workspaceId}`

Returns projects in the workspace where the authenticated user is also a project member.

### Get Project

`GET /api/v1/projects/{projectId}`

### Update Project

`PUT /api/v1/projects/{projectId}`

```json
{
  "name": "Roadmap 2026",
  "slug": "roadmap-2026",
  "description": "Updated project description",
  "status": "ACTIVE",
  "color": "#16a34a",
  "icon": "target"
}
```

### Soft Delete Project

`DELETE /api/v1/projects/{projectId}`

### Archive Project

`POST /api/v1/projects/{projectId}/archive`

### Restore Project

`POST /api/v1/projects/{projectId}/restore`

### List Project Members

`GET /api/v1/projects/{projectId}/members`

### Add Project Member

`POST /api/v1/projects/{projectId}/members`

```json
{
  "userId": "uuid",
  "role": "PROJECT_MEMBER"
}
```

### Change Project Member Role

`PATCH /api/v1/projects/{projectId}/members/{memberId}/role`

```json
{
  "role": "PROJECT_VIEWER"
}
```

### Remove Project Member

`DELETE /api/v1/projects/{projectId}/members/{memberId}`

## Events

The service layer publishes:

- `ProjectCreatedEvent`
- `ProjectUpdatedEvent`
- `ProjectArchivedEvent`
- `ProjectRestoredEvent`
- `ProjectMemberAddedEvent`
- `ProjectMemberRemovedEvent`
