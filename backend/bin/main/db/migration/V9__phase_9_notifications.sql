CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(60) NOT NULL,
    title VARCHAR(220) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    entity_type VARCHAR(80),
    entity_id UUID,
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_notifications_type CHECK (
        type IN (
            'TASK_ASSIGNED',
            'TASK_UPDATED',
            'PROJECT_CREATED',
            'SPRINT_STARTED',
            'SPRINT_COMPLETED',
            'DOCUMENT_UPDATED',
            'FILE_UPLOADED',
            'WORKSPACE_INVITATION',
            'COMMENT_ADDED',
            'MENTION'
        )
    )
);

CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(60) NOT NULL,
    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_notification_preferences_user_type UNIQUE (user_id, type),
    CONSTRAINT ck_notification_preferences_type CHECK (
        type IN (
            'TASK_ASSIGNED',
            'TASK_UPDATED',
            'PROJECT_CREATED',
            'SPRINT_STARTED',
            'SPRINT_COMPLETED',
            'DOCUMENT_UPDATED',
            'FILE_UPLOADED',
            'WORKSPACE_INVITATION',
            'COMMENT_ADDED',
            'MENTION'
        )
    )
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id, read_at, deleted_at);
CREATE INDEX idx_notifications_workspace_id ON notifications (workspace_id);
CREATE INDEX idx_notifications_project_id ON notifications (project_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
CREATE INDEX idx_notification_preferences_user_id ON notification_preferences (user_id);
