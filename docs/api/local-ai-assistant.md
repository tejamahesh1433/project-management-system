# Local AI Assistant API

Phase 14 adds a local-only AI assistant backed by Ollama and PostgreSQL RAG storage. It does not implement autonomous agents, automatic task creation, workflow automation, deployments, or mobile apps.

## Configuration

```yaml
ai:
  ollama:
    enabled: true
    base-url: http://localhost:11434
    embedding-model: nomic-embed-text
    supported-models: qwen3,gemma3,phi4-mini
```

Supported chat models:

- `qwen3`
- `gemma3`
- `phi4-mini`

Embedding model:

- `nomic-embed-text`

## Chat

`POST /api/v1/ai/chat`

```json
{
  "workspaceId": "uuid",
  "projectId": "uuid",
  "model": "QWEN3",
  "message": "Summarize project risks"
}
```

Creates or continues a conversation and stores user/assistant messages.

## Conversation History

`GET /api/v1/ai/conversations?workspaceId={workspaceId}`

Returns conversations created by the authenticated user.

## Summaries

- `POST /api/v1/ai/summarize/project/{id}`
- `POST /api/v1/ai/summarize/sprint/{id}`
- `POST /api/v1/ai/summarize/workspace/{id}`

Summaries are generated from existing workspace/project/sprint context only.

## Search

`POST /api/v1/ai/search`

```json
{
  "workspaceId": "uuid",
  "projectId": "uuid",
  "query": "authentication tasks"
}
```

Search uses PostgreSQL-stored RAG documents generated from projects, tasks, documents, activity feed, and reports.

## RAG Index

Indexed sources:

- Projects
- Tasks
- Documents
- Activity feed
- Reports

Embeddings are stored as JSON in PostgreSQL so the system remains self-hosted and does not require paid APIs.
