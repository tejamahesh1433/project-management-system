"use client";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { sprintApi, taskApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { TaskStatusBadge } from "@/components/ui/task-status";
import { Avatar } from "@/components/ui/avatar";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { useToast } from "@/components/ui/toast";
import { formatDate } from "@/lib/utils";
import { useState } from "react";
import { Plus, Play, CheckCircle, XCircle } from "lucide-react";
import type { Sprint, Task } from "@/types";

export default function SprintOverviewPage() {
  const { workspaceId, projectId, sprintId } = useParams<{ workspaceId: string; projectId: string; sprintId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();
  const [showAddTask, setShowAddTask] = useState(false);
  const [selectedTaskId, setSelectedTaskId] = useState("");

  const { data: sprint } = useQuery<Sprint>({
    queryKey: queryKeys.sprints.detail(sprintId),
    queryFn: () => sprintApi.get(sprintId),
  });

  const { data: sprintTasks = [] } = useQuery({
    queryKey: queryKeys.sprints.tasks(sprintId),
    queryFn: () => sprintApi.listTasks(sprintId),
  });

  const { data: allTasks = [] } = useQuery<Task[]>({
    queryKey: queryKeys.tasks.all(projectId),
    queryFn: () => taskApi.list(projectId),
  });

  const addedTaskIds = new Set(sprintTasks.map((st: { taskId: string }) => st.taskId));
  const availableTasks = allTasks.filter((t) => !addedTaskIds.has(t.id));

  const addTaskMutation = useMutation({
    mutationFn: () => sprintApi.addTask(sprintId, selectedTaskId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.sprints.tasks(sprintId) });
      setShowAddTask(false);
      setSelectedTaskId("");
      toast("success", "Task added to sprint");
    },
  });

  const removeTaskMutation = useMutation({
    mutationFn: (taskId: string) => sprintApi.removeTask(sprintId, taskId),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.sprints.tasks(sprintId) }),
  });

  const startMutation = useMutation({
    mutationFn: () => sprintApi.start(sprintId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.sprints.detail(sprintId) });
      toast("success", "Sprint started!");
    },
  });

  const completeMutation = useMutation({
    mutationFn: () => sprintApi.complete(sprintId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.sprints.detail(sprintId) });
      toast("success", "Sprint completed!");
    },
  });

  const tasks = sprintTasks.map((st: { task: Task }) => st.task).filter(Boolean);
  const done = tasks.filter((t: Task) => t.status === "DONE").length;
  const total = tasks.length;
  const pct = total > 0 ? Math.round((done / total) * 100) : 0;

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title={sprint?.name ?? "Sprint"} workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-3xl mx-auto space-y-6">
          {sprint && (
            <Card>
              <CardContent className="pt-6">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 flex-wrap mb-2">
                      <h2 className="text-xl font-bold">{sprint.name}</h2>
                      <Badge variant={sprint.status === "ACTIVE" ? "success" : sprint.status === "COMPLETED" ? "info" : "secondary"}>
                        {sprint.status}
                      </Badge>
                    </div>
                    {sprint.goal && <p className="text-sm text-[var(--color-muted-foreground)] mb-3">{sprint.goal}</p>}
                    <div className="flex gap-4 text-xs text-[var(--color-muted-foreground)]">
                      {sprint.startDate && <span>Start: {formatDate(sprint.startDate)}</span>}
                      {sprint.endDate && <span>End: {formatDate(sprint.endDate)}</span>}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    {sprint.status === "PLANNED" && (
                      <Button size="sm" onClick={() => startMutation.mutate()} loading={startMutation.isPending}>
                        <Play className="h-4 w-4" />Start
                      </Button>
                    )}
                    {sprint.status === "ACTIVE" && (
                      <Button size="sm" variant="secondary" onClick={() => completeMutation.mutate()} loading={completeMutation.isPending}>
                        <CheckCircle className="h-4 w-4" />Complete
                      </Button>
                    )}
                    <Button size="sm" variant="outline" onClick={() => setShowAddTask(true)}>
                      <Plus className="h-4 w-4" />Add Task
                    </Button>
                  </div>
                </div>

                <div className="mt-4 space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-[var(--color-muted-foreground)]">{done}/{total} tasks done</span>
                    <span className="font-semibold">{pct}%</span>
                  </div>
                  <div className="h-2 rounded-full bg-[var(--color-muted)] overflow-hidden">
                    <div className="h-full rounded-full bg-[var(--color-primary)] transition-all" style={{ width: `${pct}%` }} />
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Sprint Tasks ({tasks.length})</CardTitle>
              </div>
            </CardHeader>
            <CardContent className="pt-0">
              {tasks.length === 0 ? (
                <div className="text-sm text-[var(--color-muted-foreground)] py-6 text-center">
                  No tasks in this sprint.{" "}
                  <button type="button" className="text-[var(--color-primary)] hover:underline" onClick={() => setShowAddTask(true)}>
                    Add tasks
                  </button>
                </div>
              ) : (
                <div className="divide-y divide-[var(--color-border)]">
                  {tasks.map((t: Task) => (
                    <div key={t.id} className="flex items-center gap-3 py-2.5">
                      <TaskStatusBadge status={t.status} size="xs" />
                      <span className="flex-1 text-sm truncate">{t.title}</span>
                      {t.assignee && <Avatar name={t.assignee.displayName ?? t.assignee.email} src={t.assignee.avatarUrl} size="xs" />}
                      <button
                        className="text-[var(--color-muted-foreground)] hover:text-[var(--color-destructive)] transition-colors"
                        onClick={() => removeTaskMutation.mutate(t.id)}
                      >
                        <XCircle className="h-4 w-4" />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      <Modal open={showAddTask} onClose={() => setShowAddTask(false)} title="Add Task to Sprint" size="sm">
        <ModalBody>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium">Select task</label>
            <select
              value={selectedTaskId}
              onChange={(e) => setSelectedTaskId(e.target.value)}
              className="h-9 w-full rounded-md border border-[var(--color-input)] bg-[var(--color-background)] px-3 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-ring)]"
            >
              <option value="">Select a task...</option>
              {availableTasks.map((t) => (
                <option key={t.id} value={t.id}>{t.title}</option>
              ))}
            </select>
          </div>
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowAddTask(false)}>Cancel</Button>
          <Button onClick={() => addTaskMutation.mutate()} loading={addTaskMutation.isPending} disabled={!selectedTaskId}>
            Add to Sprint
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
