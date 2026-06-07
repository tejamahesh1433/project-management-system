CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(40) NOT NULL,
    title VARCHAR(220) NOT NULL,
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    sprint_id UUID REFERENCES sprints(id) ON DELETE CASCADE,
    generated_by_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    metrics_json TEXT NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_reports_type CHECK (type IN ('PROJECT', 'SPRINT', 'TEAM', 'WORKSPACE'))
);

CREATE TABLE report_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    metrics_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_generated_by_id ON reports (generated_by_id);
CREATE INDEX idx_reports_workspace_id ON reports (workspace_id);
CREATE INDEX idx_reports_project_id ON reports (project_id);
CREATE INDEX idx_reports_sprint_id ON reports (sprint_id);
CREATE INDEX idx_reports_generated_at ON reports (generated_at);
CREATE INDEX idx_report_snapshots_report_id ON report_snapshots (report_id);
CREATE INDEX idx_report_snapshots_created_at ON report_snapshots (created_at);
