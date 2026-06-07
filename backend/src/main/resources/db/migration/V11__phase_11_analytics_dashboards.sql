CREATE TABLE dashboards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    created_by_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE dashboard_widgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dashboard_id UUID NOT NULL REFERENCES dashboards(id) ON DELETE CASCADE,
    type VARCHAR(60) NOT NULL,
    title VARCHAR(160) NOT NULL,
    position INTEGER NOT NULL DEFAULT 0,
    config_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_dashboard_widgets_type CHECK (
        type IN (
            'TASK_STATUS_CHART',
            'SPRINT_PROGRESS',
            'TEAM_PERFORMANCE',
            'PROJECT_HEALTH',
            'ACTIVITY_OVERVIEW',
            'WORKSPACE_SUMMARY'
        )
    )
);

CREATE INDEX idx_dashboards_workspace_id ON dashboards (workspace_id);
CREATE INDEX idx_dashboards_project_id ON dashboards (project_id);
CREATE INDEX idx_dashboards_created_by_id ON dashboards (created_by_id);
CREATE INDEX idx_dashboard_widgets_dashboard_id ON dashboard_widgets (dashboard_id);
CREATE INDEX idx_dashboard_widgets_position ON dashboard_widgets (dashboard_id, position);
