# Phase 2 Workspace Management API

Base paths:

- `/api/v1/organizations`
- `/api/v1/workspaces`

All endpoints require `Authorization: Bearer <access-token>`.

## Scope

Implemented:

- List organizations
- Get organization
- Create organization
- Create workspace
- List workspaces for the authenticated user
- Update workspace
- Delete workspace
- Invite workspace members
- Accept workspace invitations
- List workspace invitations
- Revoke workspace invitation
- List workspace members
- Update workspace member roles
- Remove workspace member

Not implemented in Phase 2:

- Projects
- Tasks
- Boards
- Sprints
- Documents
- Files
- Reports
- Analytics
- AI

## Workspace Roles

```text
OWNER
ADMIN
MEMBER
VIEWER
```

Only organization owners can create workspaces. Workspace `OWNER` and `ADMIN` users can update workspaces and invite members. Only `OWNER` users can delete workspaces. `OWNER` cannot be assigned through invitations or role-management endpoints.

## Endpoints

### List Organizations

`GET /api/v1/organizations`

Returns organizations owned by the authenticated user.

### Get Organization

`GET /api/v1/organizations/{organizationId}`

### Create Organization

`POST /api/v1/organizations`

```json
{
  "name": "Acme",
  "slug": "acme"
}
```

### Create Workspace

`POST /api/v1/workspaces`

```json
{
  "organizationId": "uuid",
  "name": "Engineering",
  "slug": "engineering",
  "description": "Product engineering workspace"
}
```

### List Workspaces

`GET /api/v1/workspaces`

Returns workspaces where the authenticated user is a member.

### Get Workspace

`GET /api/v1/workspaces/{workspaceId}`

Returns workspace details if the authenticated user is a member.

### Update Workspace

`PUT /api/v1/workspaces/{workspaceId}`

```json
{
  "name": "Engineering Team",
  "slug": "engineering-team",
  "description": "Updated description"
}
```

### Delete Workspace

`DELETE /api/v1/workspaces/{workspaceId}`

### Invite Member

`POST /api/v1/workspaces/{workspaceId}/invitations`

```json
{
  "email": "member@example.com",
  "role": "MEMBER"
}
```

Development response includes the raw invitation token.

### List Pending Invitations

`GET /api/v1/workspaces/{workspaceId}/invitations`

### Revoke Invitation

`DELETE /api/v1/workspaces/{workspaceId}/invitations/{invitationId}`

### Accept Invitation

`POST /api/v1/workspaces/invitations/accept`

```json
{
  "token": "invitation-token"
}
```

### List Members

`GET /api/v1/workspaces/{workspaceId}/members`

### Update Member Role

`PATCH /api/v1/workspaces/{workspaceId}/members/{memberId}/role`

```json
{
  "role": "VIEWER"
}
```

### Remove Member

`DELETE /api/v1/workspaces/{workspaceId}/members/{memberId}`
