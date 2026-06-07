CREATE TABLE backup_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    size_bytes BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(40) NOT NULL,
    message VARCHAR(1000),
    created_by_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    restored_at TIMESTAMPTZ,
    CONSTRAINT ck_backup_metadata_status CHECK (status IN ('COMPLETED', 'FAILED', 'RESTORED'))
);

CREATE INDEX idx_backup_metadata_created_at ON backup_metadata (created_at);
CREATE INDEX idx_backup_metadata_created_by_id ON backup_metadata (created_by_id);
