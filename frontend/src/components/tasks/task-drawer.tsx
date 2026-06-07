"use client";
import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { taskApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Drawer } from "@/components/ui/drawer";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Select } from "@/components/ui/select";
import { Avatar } from "@/components/ui/avatar";
import { TaskStatusBadge, PriorityIcon } from "@/components/ui/task-status";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/components/ui/toast";
import { formatRelativeTime } from "@/lib/utils";
import { Send, Trash2, Edit2, Check, X } from "lucide-react";
import type { Task, TaskStatus, TaskPriority, TaskType } from "@/types";

const STATUS_OPTIONS: { value: TaskStatus; label: string }[] = [
  { value: "TODO", label: "Todo" },
  { value: "IN_PROGRESS", label: "In Progress" },
  { value: "IN_REVIEW", label: "In Review" },
  { value: "DONE", label: "Done" },
  { value: "CANCELLED", label: "Cancelled" },
];

const PRIORITY_OPTIONS: { value: TaskPriority; label: string }[] = [
  { value: "URGENT", label: "Urgent" },
  { value: "HIGH", label: "High" },
  { value: "MEDIUM", label: "Medium" },
  { value: "LOW", label: "Low" },
  { value: "NO_PRIORITY", label: "No Priority" },
];

interface TaskDrawerProps {
  taskId: string | null;
  projectId: string;
  workspaceId: string;
  onClose: () => void;
}

export function TaskDrawer({ taskId, projectId, workspaceId, onClose }: TaskDrawerProps) {
  const qc = useQueryClient();
  const { toast } = useToast();
  const [comment, setComment] = useState("");
  const [editTitle, setEditTitle] = useState(false);
  const [titleValue, setTitleValue] = useState("");

  const { data: task, isLoading } = useQuery<Task>({
    queryKey: queryKeys.tasks.detail(taskId!),
    queryFn: () => taskApi.get(taskId!),
    enabled: !!taskId,
  });

  const { data: comments = [] } = useQuery({
    queryKey: queryKeys.tasks.comments(taskId!),
    queryFn: () => taskApi.listComments(taskId!),
    enabled: !!taskId,
  });

  useEffect(() => {
    if (task) setTitleValue(task.title);
  }, [task]);

  const updateMutation = useMutation({
    mutationFn: (data: Parameters<typeof taskApi.update>[1]) => taskApi.update(taskId!, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.tasks.detail(taskId!) });
      qc.invalidateQueries({ queryKey: queryKeys.tasks.all(projectId) });
    },
  });

  const statusMutation = useMutation({
    mutationFn: (status: string) => taskApi.changeStatus(taskId!, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.tasks.detail(taskId!) });
      qc.invalidateQueries({ queryKey: queryKeys.tasks.all(projectId) });
    },
  });

  const commentMutation = useMutation({
    mutationFn: () => taskApi.createComment(taskId!, comment),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.tasks.comments(taskId!) });
      setComment("");
      toast("success", "Comment added");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => taskApi.delete(taskId!),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.tasks.all(projectId) });
      onClose();
      toast("success", "Task deleted");
    },
  });

  if (!taskId) return null;

  return (
    <Drawer open={!!taskId} onClose={onClose} size="lg">
      {isLoading ? (
        <div className="p-6 space-y-4">
          {[1,2,3].map(i => <div key={i} className="h-8 animate-pulse bg-[var(--color-muted)] rounded" />)}
        </div>
      ) : task ? (
        <div className="p-6 space-y-6">
          {/* Title */}
          <div className="space-y-1">
            {editTitle ? (
              <div className="flex items-center gap-2">
                <Input
                  value={titleValue}
                  onChange={(e) => setTitleValue(e.target.value)}
                  className="text-base font-semibold"
                  autoFocus
                />
                <Button size="icon-sm" onClick={() => { updateMutation.mutate({ title: titleValue }); setEditTitle(false); }}>
                  <Check className="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="icon-sm" onClick={() => setEditTitle(false)}>
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ) : (
              <div className="flex items-start gap-2 group">
                <h2 className="text-lg font-semibold flex-1 cursor-pointer hover:text-[var(--color-primary)]" onClick={() => setEditTitle(true)}>
                  {task.title}
                </h2>
                <Button variant="ghost" size="icon-sm" className="opacity-0 group-hover:opacity-100 shrink-0" onClick={() => setEditTitle(true)}>
                  <Edit2 className="h-3.5 w-3.5" />
                </Button>
              </div>
            )}
            <div className="flex items-center gap-2 flex-wrap">
              <TaskStatusBadge status={task.status} />
              <Badge variant="outline">{task.type}</Badge>
              <span className="flex items-center gap-1 text-xs text-[var(--color-muted-foreground)]">
                <PriorityIcon priority={task.priority} className="h-3 w-3" />
                {task.priority}
              </span>
            </div>
          </div>

          {/* Fields */}
          <div className="grid grid-cols-2 gap-4">
            <Select
              label="Status"
              value={task.status}
              options={STATUS_OPTIONS}
              onChange={(e) => statusMutation.mutate(e.target.value)}
            />
            <Select
              label="Priority"
              value={task.priority}
              options={PRIORITY_OPTIONS}
              onChange={(e) => updateMutation.mutate({ priority: e.target.value as TaskPriority })}
            />
          </div>

          {task.assignee && (
            <div>
              <p className="text-xs font-medium text-[var(--color-muted-foreground)] mb-1.5">Assignee</p>
              <div className="flex items-center gap-2">
                <Avatar name={`${task.assignee.firstName} ${task.assignee.lastName}`} src={task.assignee.avatarUrl} size="sm" />
                <span className="text-sm">{task.assignee.firstName} {task.assignee.lastName}</span>
              </div>
            </div>
          )}

          {task.description && (
            <div>
              <p className="text-xs font-medium text-[var(--color-muted-foreground)] mb-1.5">Description</p>
              <p className="text-sm text-[var(--color-foreground)] whitespace-pre-wrap">{task.description}</p>
            </div>
          )}

          {/* Comments */}
          <div>
            <p className="text-xs font-medium text-[var(--color-muted-foreground)] mb-3">
              Comments ({comments.length})
            </p>
            <div className="space-y-3 mb-3">
              {comments.map((c: { id: string; author?: { firstName?: string; avatarUrl?: string }; content: string; createdAt: string }) => (
                <div key={c.id} className="flex items-start gap-2.5">
                  <Avatar name={c.author?.firstName ?? "?"} src={c.author?.avatarUrl} size="xs" className="mt-0.5" />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-medium">{c.author?.firstName}</span>
                      <span className="text-[10px] text-[var(--color-muted-foreground)]">{formatRelativeTime(c.createdAt)}</span>
                    </div>
                    <p className="text-sm text-[var(--color-foreground)] mt-0.5">{c.content}</p>
                  </div>
                </div>
              ))}
            </div>
            <div className="flex items-end gap-2">
              <Textarea
                placeholder="Add a comment..."
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                className="min-h-[60px]"
                onKeyDown={(e) => {
                  if (e.key === "Enter" && (e.metaKey || e.ctrlKey) && comment.trim()) {
                    commentMutation.mutate();
                  }
                }}
              />
              <Button
                size="icon"
                onClick={() => commentMutation.mutate()}
                disabled={!comment.trim()}
                loading={commentMutation.isPending}
              >
                <Send className="h-4 w-4" />
              </Button>
            </div>
          </div>

          <div className="pt-2 border-t border-[var(--color-border)]">
            <Button
              variant="destructive"
              size="sm"
              onClick={() => deleteMutation.mutate()}
              loading={deleteMutation.isPending}
            >
              <Trash2 className="h-4 w-4" />
              Delete Task
            </Button>
          </div>
        </div>
      ) : null}
    </Drawer>
  );
}
