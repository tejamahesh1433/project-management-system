CREATE TABLE boards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    template VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_boards_template CHECK (template IN ('SCRUM', 'KANBAN'))
);

CREATE TABLE board_columns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    board_id UUID NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE board_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    board_id UUID NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    column_id UUID NOT NULL REFERENCES board_columns(id) ON DELETE CASCADE,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_board_tasks_board_task UNIQUE (board_id, task_id)
);

CREATE INDEX idx_boards_project_id ON boards (project_id);
CREATE INDEX idx_boards_deleted_at ON boards (deleted_at);
CREATE INDEX idx_board_columns_board_id ON board_columns (board_id);
CREATE INDEX idx_board_columns_board_position ON board_columns (board_id, position);
CREATE INDEX idx_board_tasks_board_id ON board_tasks (board_id);
CREATE INDEX idx_board_tasks_column_id ON board_tasks (column_id);
CREATE INDEX idx_board_tasks_column_position ON board_tasks (column_id, position);
CREATE INDEX idx_board_tasks_task_id ON board_tasks (task_id);
