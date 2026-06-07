# Phase 7 Documents & File Management API

Base path: `/api/v1`

All endpoints require `Authorization: Bearer <access-token>`.

## Scope

Implemented:

- Folder hierarchy
- Document CRUD
- Document status: `DRAFT`, `PUBLISHED`, `ARCHIVED`
- Document versioning
- Restore previous version
- File upload metadata
- Local storage path metadata

Not implemented:

- AI generation
- Realtime collaboration
- Reports
- Analytics
- Notifications
- Integrations

## Endpoints

### Folders

`POST /api/v1/folders`

```json
{
  "projectId": "uuid",
  "parentFolderId": "uuid",
  "name": "Specs"
}
```

`GET /api/v1/folders?projectId={projectId}`

`PUT /api/v1/folders/{folderId}`

`DELETE /api/v1/folders/{folderId}`

### Documents

`POST /api/v1/documents`

```json
{
  "projectId": "uuid",
  "folderId": "uuid",
  "title": "Project Brief",
  "content": "{\"type\":\"doc\",\"content\":[]}"
}
```

`GET /api/v1/documents?projectId={projectId}`

`GET /api/v1/documents/{documentId}`

`PUT /api/v1/documents/{documentId}`

```json
{
  "folderId": "uuid",
  "title": "Project Brief",
  "content": "{\"type\":\"doc\",\"content\":[]}",
  "status": "PUBLISHED"
}
```

`DELETE /api/v1/documents/{documentId}`

`GET /api/v1/documents/{documentId}/versions`

`POST /api/v1/documents/{documentId}/versions/{versionNumber}/restore`

### Files

`POST /api/v1/files`

```json
{
  "projectId": "uuid",
  "folderId": "uuid",
  "fileName": "brief.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 12345
}
```

`GET /api/v1/files?projectId={projectId}`

`GET /api/v1/files/{fileId}`

`DELETE /api/v1/files/{fileId}`

## Events

- `DocumentCreatedEvent`
- `DocumentUpdatedEvent`
- `DocumentPublishedEvent`
- `DocumentVersionCreatedEvent`
- `FileUploadedEvent`
- `FileDeletedEvent`
