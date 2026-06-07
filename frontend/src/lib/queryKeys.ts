export const queryKeys = {
  workspaces: {
    all: ["workspaces"] as const,
    detail: (id: string) => ["workspaces", id] as const,
    members: (id: string) => ["workspaces", id, "members"] as const,
    invitations: (id: string) => ["workspaces", id, "invitations"] as const,
  },
  projects: {
    all: (workspaceId: string) => ["projects", workspaceId] as const,
    detail: (id: string) => ["projects", "detail", id] as const,
    members: (id: string) => ["projects", id, "members"] as const,
  },
  tasks: {
    all: (projectId: string) => ["tasks", projectId] as const,
    detail: (id: string) => ["tasks", "detail", id] as const,
    comments: (taskId: string) => ["tasks", taskId, "comments"] as const,
    labels: (projectId: string) => ["labels", projectId] as const,
  },
  boards: {
    all: (projectId: string) => ["boards", projectId] as const,
    detail: (id: string) => ["boards", "detail", id] as const,
  },
  sprints: {
    all: (projectId: string) => ["sprints", projectId] as const,
    detail: (id: string) => ["sprints", "detail", id] as const,
    tasks: (id: string) => ["sprints", id, "tasks"] as const,
    metrics: (id: string) => ["sprints", id, "metrics"] as const,
  },
  documents: {
    folders: (projectId: string) => ["folders", projectId] as const,
    all: (projectId: string) => ["documents", projectId] as const,
    detail: (id: string) => ["documents", "detail", id] as const,
    versions: (id: string) => ["documents", id, "versions"] as const,
  },
  files: {
    all: (projectId: string) => ["files", projectId] as const,
  },
  notifications: {
    all: ["notifications"] as const,
    unread: ["notifications", "unread"] as const,
  },
  activity: {
    workspace: (workspaceId: string) => ["activity", "workspace", workspaceId] as const,
    project: (projectId: string) => ["activity", "project", projectId] as const,
  },
  analytics: {
    workspace: (workspaceId: string) => ["analytics", "workspace", workspaceId] as const,
    project: (projectId: string) => ["analytics", "project", projectId] as const,
  },
  reports: {
    all: (workspaceId: string) => ["reports", workspaceId] as const,
    detail: (id: string) => ["reports", "detail", id] as const,
  },
  integrations: {
    all: (workspaceId: string) => ["integrations", workspaceId] as const,
    detail: (id: string) => ["integrations", "detail", id] as const,
  },
  ai: {
    conversations: (workspaceId: string) => ["ai", "conversations", workspaceId] as const,
  },
};
