CREATE TABLE ai_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    scope VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    model VARCHAR(40) NOT NULL,
    created_by_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_ai_conversations_scope CHECK (scope IN ('WORKSPACE', 'PROJECT')),
    CONSTRAINT ck_ai_conversations_model CHECK (model IN ('QWEN3', 'GEMMA3', 'PHI4_MINI'))
);

CREATE TABLE ai_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    role VARCHAR(40) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_ai_messages_role CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM'))
);

CREATE TABLE ai_rag_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    source_type VARCHAR(40) NOT NULL,
    source_id UUID NOT NULL,
    title VARCHAR(220) NOT NULL,
    content TEXT NOT NULL,
    embedding_json TEXT NOT NULL,
    indexed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ai_rag_source UNIQUE (source_type, source_id),
    CONSTRAINT ck_ai_rag_source_type CHECK (source_type IN ('PROJECT', 'TASK', 'DOCUMENT', 'ACTIVITY', 'REPORT'))
);

CREATE INDEX idx_ai_conversations_workspace_id ON ai_conversations (workspace_id);
CREATE INDEX idx_ai_conversations_created_by_id ON ai_conversations (created_by_id);
CREATE INDEX idx_ai_messages_conversation_id ON ai_messages (conversation_id);
CREATE INDEX idx_ai_rag_documents_workspace_id ON ai_rag_documents (workspace_id);
CREATE INDEX idx_ai_rag_documents_project_id ON ai_rag_documents (project_id);
CREATE INDEX idx_ai_rag_documents_source ON ai_rag_documents (source_type, source_id);
