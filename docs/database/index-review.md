# RC Index Review

## Added In V15

- `workspace_members(workspace_id, user_id)`
- `project_members(project_id, user_id)`
- `tasks(project_id, status, deleted_at)`
- `tasks(project_id, assignee_id, deleted_at)`
- `task_comments(task_id, created_at)`
- `board_tasks(board_id, board_column_id, position)`
- `sprints(project_id, status, deleted_at)`
- `sprint_tasks(sprint_id, task_id)`
- `documents(project_id, status, deleted_at)`
- `file_assets(project_id, created_at, deleted_at)`
- `notifications(user_id, created_at, deleted_at)`
- `reports(workspace_id, generated_at)`
- `ai_conversations(workspace_id, updated_at)`
- `ai_rag_documents(workspace_id, source_type)`
- `integrations(workspace_id, type)`
- `webhook_subscriptions(provider, enabled)`

These support authorization lookups, listing views, analytics, AI search filtering, and webhook dispatch.
