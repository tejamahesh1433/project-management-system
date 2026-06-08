// Auth
export interface User {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  createdAt?: string;
  // legacy aliases — backend returns displayName only
  firstName?: string;
  lastName?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user?: User;
}

// Workspace
export interface Workspace {
  id: string;
  name: string;
  slug: string;
  description?: string;
  logoUrl?: string;
  ownerId: string;
  createdAt: string;
  updatedAt: string;
}

export interface WorkspaceMember {
  id: string;
  workspaceId: string;
  userId: string;
  email: string;
  displayName: string;
  role: "OWNER" | "ADMIN" | "MEMBER" | "VIEWER";
  createdAt: string;
}

export interface WorkspaceInvitation {
  id: string;
  workspaceId: string;
  email: string;
  role: string;
  status: "PENDING" | "ACCEPTED" | "REVOKED";
  token: string;
  expiresAt: string;
  createdAt: string;
}

// Project
export interface Project {
  id: string;
  workspaceId: string;
  name: string;
  description?: string;
  status: "ACTIVE" | "ARCHIVED" | "DELETED";
  color?: string;
  iconUrl?: string;
  leadId?: string;
  startDate?: string;
  endDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectMember {
  id: string;
  projectId: string;
  userId: string;
  email: string;
  displayName: string;
  role: "LEAD" | "MEMBER" | "VIEWER";
  joinedAt: string;
}

// Task
export type TaskStatus = "TODO" | "IN_PROGRESS" | "IN_REVIEW" | "DONE" | "CANCELLED";
export type TaskPriority = "URGENT" | "HIGH" | "MEDIUM" | "LOW" | "NO_PRIORITY";
export type TaskType = "TASK" | "BUG" | "FEATURE" | "IMPROVEMENT" | "EPIC" | "STORY" | "SUBTASK";

export interface Task {
  id: string;
  projectId: string;
  sprintId?: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  type: TaskType;
  assigneeId?: string;
  assignee?: User;
  reporterId: string;
  reporter?: User;
  labels?: Label[];
  dueDate?: string;
  storyPoints?: number;
  position: number;
  createdAt: string;
  updatedAt: string;
}

export interface TaskComment {
  id: string;
  taskId: string;
  authorId: string;
  author: User;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface Label {
  id: string;
  projectId: string;
  name: string;
  color: string;
  createdAt: string;
}

// Board
export interface Board {
  id: string;
  projectId: string;
  name: string;
  description?: string;
  columns: BoardColumn[];
  createdAt: string;
  updatedAt: string;
}

export interface BoardColumn {
  id: string;
  boardId: string;
  name: string;
  taskStatus: TaskStatus;
  position: number;
  tasks: Task[];
  createdAt: string;
}

// Sprint
export type SprintStatus = "PLANNED" | "ACTIVE" | "COMPLETED" | "CANCELLED";

export interface Sprint {
  id: string;
  projectId: string;
  name: string;
  goal?: string;
  status: SprintStatus;
  startDate?: string;
  endDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SprintTask {
  id: string;
  sprintId: string;
  taskId: string;
  task: Task;
  addedAt: string;
}

export interface SprintMetrics {
  sprintId: string;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  completionRate: number;
  totalEstimatedHours?: number;
  completedEstimatedHours?: number;
}

// Document
export interface Folder {
  id: string;
  projectId: string;
  parentId?: string;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface Document {
  id: string;
  projectId: string;
  folderId?: string;
  title: string;
  content?: string;
  authorId: string;
  author?: User;
  currentVersion: number;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentVersion {
  id: string;
  documentId: string;
  versionNumber: number;
  content?: string;
  authorId: string;
  author?: User;
  createdAt: string;
}

// File
export interface FileAsset {
  id: string;
  projectId: string;
  folderId?: string;
  name: string;
  originalName: string;
  mimeType: string;
  size: number;
  url: string;
  uploadedById: string;
  uploadedBy?: User;
  createdAt: string;
}

// Activity
export interface Activity {
  id: string;
  workspaceId: string;
  projectId?: string;
  actorId: string;
  actor?: User;
  action: string;
  entityType: string;
  entityId: string;
  metadata?: Record<string, unknown>;
  createdAt: string;
}

// Notification
export interface Notification {
  id: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  read: boolean;
  entityType?: string;
  entityId?: string;
  metadata?: Record<string, unknown>;
  createdAt: string;
}

export interface UnreadCount {
  count: number;
}

// Analytics
export interface AnalyticsSummary {
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  cancelledTasks: number;
  overdueTasks: number;
  completionRate: number;
  tasksByPriority: Record<string, number>;
  tasksByStatus: Record<string, number>;
  tasksByType: Record<string, number>;
}

// Report
export interface Report {
  id: string;
  workspaceId: string;
  name: string;
  type: string;
  filters?: Record<string, unknown>;
  createdById: string;
  createdAt: string;
  updatedAt: string;
}

// Dashboard
export interface Dashboard {
  id: string;
  workspaceId: string;
  name: string;
  widgets: DashboardWidget[];
  createdAt: string;
}

export interface DashboardWidget {
  id: string;
  dashboardId: string;
  type: string;
  title: string;
  config?: Record<string, unknown>;
  position: { x: number; y: number; w: number; h: number };
}

// Integration
export interface Integration {
  id: string;
  workspaceId: string;
  type: "GITHUB" | "GITLAB" | "SLACK" | "JIRA" | "WEBHOOK" | string;
  name: string;
  config?: Record<string, unknown>;
  status: "ACTIVE" | "INACTIVE" | "ERROR";
  createdAt: string;
}

export interface IntegrationTestResult {
  success: boolean;
  message: string;
}

// AI
export interface AiConversation {
  id: string;
  workspaceId: string;
  userId: string;
  title: string;
  messages: AiMessage[];
  createdAt: string;
}

export interface AiMessage {
  role: "USER" | "ASSISTANT";
  content: string;
  timestamp?: string;
}

export interface AiChatResponse {
  conversationId: string;
  message: string;
  role: string;
}

export interface AiSummary {
  summary: string;
  insights?: string[];
}

export interface AiSearchResult {
  results: Array<{
    type: string;
    id: string;
    title: string;
    excerpt: string;
    relevanceScore: number;
  }>;
}

// Pagination
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// API Error
export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}
