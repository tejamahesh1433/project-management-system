ALTER TABLE tasks
    ADD COLUMN story_points INTEGER NOT NULL DEFAULT 0;

CREATE TABLE sprints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    goal VARCHAR(1000),
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_sprints_status CHECK (status IN ('PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_sprints_dates CHECK (end_date >= start_date)
);

CREATE UNIQUE INDEX uq_sprints_one_active_per_project
    ON sprints (project_id)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;

CREATE TABLE sprint_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sprint_id UUID NOT NULL REFERENCES sprints(id) ON DELETE CASCADE,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_sprint_tasks_sprint_task UNIQUE (sprint_id, task_id)
);

CREATE INDEX idx_sprints_project_id ON sprints (project_id);
CREATE INDEX idx_sprints_status ON sprints (status);
CREATE INDEX idx_sprints_deleted_at ON sprints (deleted_at);
CREATE INDEX idx_sprint_tasks_sprint_id ON sprint_tasks (sprint_id);
CREATE INDEX idx_sprint_tasks_task_id ON sprint_tasks (task_id);
