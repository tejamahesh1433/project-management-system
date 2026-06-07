# Backup Recovery Validation

## Tested

- Manual backup endpoint creates backup metadata.
- Backup history endpoint returns the created record.
- Backup download endpoint is implemented.
- Restore endpoint validates configured restore command.

## Operator Workflow

1. Set `BACKUP_DIRECTORY`.
2. Set `PG_DUMP_COMMAND`.
3. Set `PG_RESTORE_COMMAND`.
4. Create a manual backup with `POST /api/v1/backups`.
5. Download with `GET /api/v1/backups/{id}/download`.
6. Restore in a staging clone first with `POST /api/v1/backups/{id}/restore`.

## RC Assessment

Backup metadata and API workflow are ready. Full restore should be validated on the Ubuntu server with real PostgreSQL credentials before production cutover.
