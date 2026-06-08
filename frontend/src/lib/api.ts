import axios, { AxiosError } from "axios";

const api = axios.create({
  baseURL: "/api/v1",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("accessToken");
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      const refreshToken = localStorage.getItem("refreshToken");
      if (refreshToken && !error.config?.url?.includes("/auth/refresh")) {
        try {
          const res = await axios.post("/api/v1/auth/refresh", { refreshToken });
          const { accessToken } = res.data;
          localStorage.setItem("accessToken", accessToken);
          if (error.config) {
            error.config.headers.Authorization = `Bearer ${accessToken}`;
            return api.request(error.config);
          }
        } catch {
          localStorage.removeItem("accessToken");
          localStorage.removeItem("refreshToken");
          window.location.href = "/login";
        }
      } else {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

// Auth
export const authApi = {
  login: (email: string, password: string) =>
    api.post("/auth/login", { email, password }).then((r) => r.data),
  register: (data: { email: string; password: string; displayName: string }) =>
    api.post("/auth/register", data).then((r) => r.data),
  logout: (refreshToken: string) =>
    api.post("/auth/logout", { refreshToken }, {
      headers: { Authorization: `Bearer ${localStorage.getItem("accessToken")}` },
    }).then((r) => r.data),
  forgotPassword: (email: string) =>
    api.post("/auth/forgot-password", { email }).then((r) => r.data),
  resetPassword: (token: string, newPassword: string) =>
    api.post("/auth/reset-password", { token, newPassword }).then((r) => r.data),
};

// Organizations
export const organizationApi = {
  list: () => api.get("/organizations").then((r) => r.data),
  get: (id: string) => api.get(`/organizations/${id}`).then((r) => r.data),
  create: (data: { name: string; slug: string }) =>
    api.post("/organizations", data).then((r) => r.data),
};

// Workspaces
export const workspaceApi = {
  list: () => api.get("/workspaces").then((r) => r.data),
  get: (id: string) => api.get(`/workspaces/${id}`).then((r) => r.data),
  create: (data: { organizationId: string; name: string; slug: string; description?: string }) =>
    api.post("/workspaces", data).then((r) => r.data),
  update: (id: string, data: { name?: string; description?: string }) =>
    api.put(`/workspaces/${id}`, data).then((r) => r.data),
  delete: (id: string) => api.delete(`/workspaces/${id}`).then((r) => r.data),
  listMembers: (id: string) => api.get(`/workspaces/${id}/members`).then((r) => r.data),
  inviteMember: (id: string, data: { email: string; role: string }) =>
    api.post(`/workspaces/${id}/invitations`, data).then((r) => r.data),
  listInvitations: (id: string) =>
    api.get(`/workspaces/${id}/invitations`).then((r) => r.data),
  revokeInvitation: (workspaceId: string, invitationId: string) =>
    api.delete(`/workspaces/${workspaceId}/invitations/${invitationId}`).then((r) => r.data),
  updateMemberRole: (workspaceId: string, memberId: string, role: string) =>
    api.patch(`/workspaces/${workspaceId}/members/${memberId}/role`, { role }).then((r) => r.data),
  removeMember: (workspaceId: string, memberId: string) =>
    api.delete(`/workspaces/${workspaceId}/members/${memberId}`).then((r) => r.data),
};

// Projects
export const projectApi = {
  list: (workspaceId: string) =>
    api.get("/projects", { params: { workspaceId } }).then((r) => r.data),
  get: (id: string) => api.get(`/projects/${id}`).then((r) => r.data),
  create: (data: { workspaceId: string; name: string; slug: string; description?: string; color?: string; icon?: string }) =>
    api.post("/projects", data).then((r) => r.data),
  update: (id: string, data: Partial<{ name: string; slug: string; description: string; color: string; status: string; icon: string }>) =>
    api.put(`/projects/${id}`, data).then((r) => r.data),
  delete: (id: string) => api.delete(`/projects/${id}`).then((r) => r.data),
  archive: (id: string) => api.post(`/projects/${id}/archive`).then((r) => r.data),
  restore: (id: string) => api.post(`/projects/${id}/restore`).then((r) => r.data),
  listMembers: (id: string) => api.get(`/projects/${id}/members`).then((r) => r.data),
  addMember: (id: string, data: { userId: string; role: string }) =>
    api.post(`/projects/${id}/members`, data).then((r) => r.data),
  updateMemberRole: (projectId: string, memberId: string, role: string) =>
    api.patch(`/projects/${projectId}/members/${memberId}/role`, { role }).then((r) => r.data),
  removeMember: (projectId: string, memberId: string) =>
    api.delete(`/projects/${projectId}/members/${memberId}`).then((r) => r.data),
};

// Tasks
export const taskApi = {
  list: (projectId: string) =>
    api.get("/tasks", { params: { projectId } }).then((r) => r.data),
  get: (id: string) => api.get(`/tasks/${id}`).then((r) => r.data),
  create: (data: {
    projectId: string;
    title: string;
    description?: string;
    status?: string;
    priority?: string;
    type?: string;
    assigneeId?: string;
    sprintId?: string;
    dueDate?: string;
    storyPoints?: number;
  }) => api.post("/tasks", data).then((r) => r.data),
  update: (id: string, data: Partial<{
    title: string;
    description: string;
    priority: string;
    type: string;
    dueDate: string;
    storyPoints: number;
    sprintId: string;
  }>) => api.put(`/tasks/${id}`, data).then((r) => r.data),
  delete: (id: string) => api.delete(`/tasks/${id}`).then((r) => r.data),
  assign: (id: string, assigneeId: string | null) =>
    api.patch(`/tasks/${id}/assignee`, { assigneeId }).then((r) => r.data),
  changeStatus: (id: string, status: string) =>
    api.patch(`/tasks/${id}/status`, { status }).then((r) => r.data),
  listComments: (id: string) =>
    api.get(`/tasks/${id}/comments`).then((r) => r.data),
  createComment: (id: string, content: string) =>
    api.post(`/tasks/${id}/comments`, { content }).then((r) => r.data),
  updateComment: (taskId: string, commentId: string, content: string) =>
    api.put(`/tasks/${taskId}/comments/${commentId}`, { content }).then((r) => r.data),
  listLabels: (projectId: string) =>
    api.get("/labels", { params: { projectId } }).then((r) => r.data),
  createLabel: (data: { projectId: string; name: string; color: string }) =>
    api.post("/labels", data).then((r) => r.data),
  addLabel: (taskId: string, labelId: string) =>
    api.post(`/tasks/${taskId}/labels`, { labelId }).then((r) => r.data),
  removeLabel: (taskId: string, labelId: string) =>
    api.delete(`/tasks/${taskId}/labels/${labelId}`).then((r) => r.data),
};

// Boards
export const boardApi = {
  list: (projectId: string) =>
    api.get("/boards", { params: { projectId } }).then((r) => r.data),
  get: (id: string) => api.get(`/boards/${id}`).then((r) => r.data),
  create: (data: { projectId: string; name: string; description?: string; template: string }) =>
    api.post("/boards", data).then((r) => r.data),
  update: (id: string, data: { name?: string; description?: string }) =>
    api.put(`/boards/${id}`, data).then((r) => r.data),
  delete: (id: string) => api.delete(`/boards/${id}`).then((r) => r.data),
  createColumn: (boardId: string, data: { name: string; taskStatus: string; position?: number }) =>
    api.post(`/boards/${boardId}/columns`, data).then((r) => r.data),
  updateColumn: (boardId: string, columnId: string, data: { name?: string; position?: number }) =>
    api.put(`/boards/${boardId}/columns/${columnId}`, data).then((r) => r.data),
  deleteColumn: (boardId: string, columnId: string) =>
    api.delete(`/boards/${boardId}/columns/${columnId}`).then((r) => r.data),
  moveTask: (boardId: string, data: { taskId: string; targetColumnId: string; position?: number }) =>
    api.patch(`/boards/${boardId}/tasks/move`, data).then((r) => r.data),
};

// Sprints
export const sprintApi = {
  list: (projectId: string) =>
    api.get("/sprints", { params: { projectId } }).then((r) => r.data),
  get: (id: string) => api.get(`/sprints/${id}`).then((r) => r.data),
  create: (data: { projectId: string; name: string; goal?: string; startDate?: string; endDate?: string }) =>
    api.post("/sprints", data).then((r) => r.data),
  update: (id: string, data: Partial<{ name: string; goal: string; startDate: string; endDate: string }>) =>
    api.put(`/sprints/${id}`, data).then((r) => r.data),
  delete: (id: string) => api.delete(`/sprints/${id}`).then((r) => r.data),
  start: (id: string) => api.post(`/sprints/${id}/start`).then((r) => r.data),
  complete: (id: string) => api.post(`/sprints/${id}/complete`).then((r) => r.data),
  cancel: (id: string) => api.post(`/sprints/${id}/cancel`).then((r) => r.data),
  listTasks: (id: string) => api.get(`/sprints/${id}/tasks`).then((r) => r.data),
  addTask: (id: string, taskId: string) =>
    api.post(`/sprints/${id}/tasks`, { taskId }).then((r) => r.data),
  removeTask: (id: string, taskId: string) =>
    api.delete(`/sprints/${id}/tasks/${taskId}`).then((r) => r.data),
  metrics: (id: string) => api.get(`/sprints/${id}/metrics`).then((r) => r.data),
};

// Documents
export const documentApi = {
  listFolders: (projectId: string) =>
    api.get("/folders", { params: { projectId } }).then((r) => r.data),
  createFolder: (data: { projectId: string; name: string; parentId?: string }) =>
    api.post("/folders", data).then((r) => r.data),
  updateFolder: (id: string, data: { name?: string; parentId?: string }) =>
    api.put(`/folders/${id}`, data).then((r) => r.data),
  deleteFolder: (id: string) => api.delete(`/folders/${id}`).then((r) => r.data),
  listDocuments: (projectId: string) =>
    api.get("/documents", { params: { projectId } }).then((r) => r.data),
  getDocument: (id: string) => api.get(`/documents/${id}`).then((r) => r.data),
  createDocument: (data: { projectId: string; title: string; content?: string; folderId?: string }) =>
    api.post("/documents", data).then((r) => r.data),
  updateDocument: (id: string, data: { title?: string; content?: string; folderId?: string }) =>
    api.put(`/documents/${id}`, data).then((r) => r.data),
  deleteDocument: (id: string) => api.delete(`/documents/${id}`).then((r) => r.data),
  getVersions: (id: string) => api.get(`/documents/${id}/versions`).then((r) => r.data),
  restoreVersion: (documentId: string, versionNumber: number) =>
    api.post(`/documents/${documentId}/versions/${versionNumber}/restore`).then((r) => r.data),
};

// Files
export const fileApi = {
  list: (projectId: string, folderId?: string) =>
    api.get("/files", { params: { projectId, folderId } }).then((r) => r.data),
  upload: (projectId: string, file: File, folderId?: string) => {
    const form = new FormData();
    form.append("file", file);
    form.append("projectId", projectId);
    if (folderId) form.append("folderId", folderId);
    return api.post("/files", form, {
      headers: { "Content-Type": "multipart/form-data" },
    }).then((r) => r.data);
  },
  delete: (id: string) => api.delete(`/files/${id}`).then((r) => r.data),
};

// Activity
export const activityApi = {
  workspace: (workspaceId: string) =>
    api.get(`/activity/workspaces/${workspaceId}`).then((r) => r.data),
  project: (projectId: string) =>
    api.get(`/activity/projects/${projectId}`).then((r) => r.data),
};

// Notifications
export const notificationApi = {
  list: () => api.get("/notifications").then((r) => r.data),
  unread: () => api.get("/notifications/unread").then((r) => r.data),
  markRead: (id: string) => api.patch(`/notifications/${id}/read`).then((r) => r.data),
  markAllRead: () => api.patch("/notifications/read-all").then((r) => r.data),
  delete: (id: string) => api.delete(`/notifications/${id}`).then((r) => r.data),
  getPreferences: () => api.get("/notification-preferences").then((r) => r.data),
  updatePreferences: (preferences: Array<{ type: string; enabled: boolean }>) =>
    api.put("/notification-preferences", { preferences }).then((r) => r.data),
};

// Analytics
export const analyticsApi = {
  workspace: (workspaceId: string, params?: Record<string, unknown>) =>
    api.get(`/analytics/workspaces/${workspaceId}`, { params }).then((r) => r.data),
  project: (projectId: string, params?: Record<string, unknown>) =>
    api.get(`/analytics/projects/${projectId}`, { params }).then((r) => r.data),
};

// Reports
export const reportApi = {
  list: (workspaceId: string) =>
    api.get("/reports", { params: { workspaceId } }).then((r) => r.data),
  get: (id: string) => api.get(`/reports/${id}`).then((r) => r.data),
  create: (data: { workspaceId: string; name: string; type: string; filters?: Record<string, unknown> }) =>
    api.post("/reports", data).then((r) => r.data),
  export: (id: string, format: string) =>
    api.get(`/reports/${id}/export`, { params: { format }, responseType: "blob" }).then((r) => r.data),
};

// Integrations
export const integrationApi = {
  list: (workspaceId: string) =>
    api.get("/integrations", { params: { workspaceId } }).then((r) => r.data),
  get: (id: string) => api.get(`/integrations/${id}`).then((r) => r.data),
  create: (data: { workspaceId: string; type: string; name: string; config?: Record<string, unknown> }) =>
    api.post("/integrations", data).then((r) => r.data),
  delete: (id: string) => api.delete(`/integrations/${id}`).then((r) => r.data),
  test: (id: string) => api.post(`/integrations/${id}/test`).then((r) => r.data),
};

// AI
export const aiApi = {
  chat: (workspaceId: string, message: string, conversationId?: string) =>
    api.post("/ai/chat", { workspaceId, message, conversationId }).then((r) => r.data),
  conversations: (workspaceId: string) =>
    api.get("/ai/conversations", { params: { workspaceId } }).then((r) => r.data),
  summarizeProject: (id: string) =>
    api.post(`/ai/summarize/project/${id}`).then((r) => r.data),
  summarizeSprint: (id: string) =>
    api.post(`/ai/summarize/sprint/${id}`).then((r) => r.data),
  summarizeWorkspace: (id: string) =>
    api.post(`/ai/summarize/workspace/${id}`).then((r) => r.data),
  search: (workspaceId: string, query: string) =>
    api.post("/ai/search", { workspaceId, query }).then((r) => r.data),
};

export default api;
