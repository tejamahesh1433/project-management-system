# RC Database Review

## Foreign Keys And Cascades

- Workspace-owned data cascades from `workspaces`.
- Project-owned data cascades from `projects`.
- User ownership uses `ON DELETE RESTRICT` where auditability matters.
- Backup metadata is restricted to preserve operator history.
- AI conversations/messages cascade within conversation scope.

## Constraints

- Slugs are scoped and unique where required.
- Enum columns use check constraints.
- RAG documents have `UNIQUE (source_type, source_id)` for idempotent indexing.

## RC Changes

- Added V15 index migration for permission checks, common task/document queries, notifications, AI RAG, integrations, and webhooks.

## Remaining Recommendations

- Consider PostgreSQL `pgvector` only if local deployment accepts an additional extension.
- Add retention policies for audit logs, activities, notifications, and AI messages before very large production use.
