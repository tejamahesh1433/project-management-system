CREATE TABLE integrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    type VARCHAR(40) NOT NULL,
    name VARCHAR(160) NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    repository_url VARCHAR(500),
    repository_name VARCHAR(220),
    metadata_json TEXT,
    created_by_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_integrations_type CHECK (
        type IN ('GITHUB', 'GITLAB', 'GITEA', 'JENKINS', 'DOCKER', 'KUBERNETES', 'DISCORD', 'TELEGRAM', 'SMTP')
    ),
    CONSTRAINT ck_integrations_status CHECK (status IN ('ACTIVE', 'DISABLED', 'ERROR'))
);

CREATE TABLE integration_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    integration_id UUID NOT NULL REFERENCES integrations(id) ON DELETE CASCADE,
    endpoint_url VARCHAR(500) NOT NULL,
    external_id VARCHAR(120),
    status VARCHAR(40) NOT NULL DEFAULT 'NOT_TESTED',
    last_message VARCHAR(1000),
    last_checked_at TIMESTAMPTZ,
    CONSTRAINT ck_integration_connections_status CHECK (status IN ('NOT_TESTED', 'CONNECTED', 'FAILED'))
);

CREATE TABLE webhook_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    integration_id UUID NOT NULL REFERENCES integrations(id) ON DELETE CASCADE,
    provider VARCHAR(80) NOT NULL,
    secret_hash VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_received_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_integrations_workspace_id ON integrations (workspace_id);
CREATE INDEX idx_integrations_project_id ON integrations (project_id);
CREATE INDEX idx_integrations_type ON integrations (type);
CREATE INDEX idx_integration_connections_integration_id ON integration_connections (integration_id);
CREATE INDEX idx_webhook_subscriptions_integration_id ON webhook_subscriptions (integration_id);
CREATE INDEX idx_webhook_subscriptions_provider ON webhook_subscriptions (provider);
