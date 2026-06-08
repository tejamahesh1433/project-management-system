"use client";
import { useState, useMemo } from "react";
import { useDebounce } from "@/lib/hooks";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { taskApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { TaskStatusBadge, PriorityIcon } from "@/components/ui/task-status";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { CreateTaskModal } from "@/components/tasks/create-task-modal";
import { TaskDrawer } from "@/components/tasks/task-drawer";
import { useToast } from "@/components/ui/toast";
import { cn, formatDate } from "@/lib/utils";
import { Plus, Search, ListTodo, SlidersHorizontal, ChevronDown } from "lucide-react";
import type { Task, TaskStatus, TaskPriority, TaskType } from "@/types";

const STATUS_FILTER: { value: TaskStatus | "ALL"; label: string }[] = [
  { value: "ALL", label: "All" },
  { value: "TODO", label: "Todo" },
  { value: "IN_PROGRESS", label: "In Progress" },
  { value: "IN_REVIEW", label: "In Review" },
  { value: "DONE", label: "Done" },
  { value: "CANCELLED", label: "Cancelled" },
];

export default function ProjectTasksPage() {
  const { workspaceId, projectId } = useParams<{ workspaceId: string; projectId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();

  const [search, setSearch] = useState("");
  const debouncedSearch = useDebounce(search, 200);
  const [statusFilter, setStatusFilter] = useState<TaskStatus | "ALL">("ALL");
  const [priorityFilter, setPriorityFilter] = useState<TaskPriority | "ALL">("ALL");
  const [showCreate, setShowCreate] = useState(false);
  const [selectedTaskId, setSelectedTaskId] = useState<string | null>(null);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());

  const { data: tasks = [], isLoading } = useQuery<Task[]>({
    queryKey: queryKeys.tasks.all(projectId),
    queryFn: () => taskApi.list(projectId),
  });

  const filtered = useMemo(() => {
    return tasks.filter((t) => {
      const matchSearch = !debouncedSearch || t.title.toLowerCase().includes(debouncedSearch.toLowerCase());
      const matchStatus = statusFilter === "ALL" || t.status === statusFilter;
      const matchPriority = priorityFilter === "ALL" || t.priority === priorityFilter;
      return matchSearch && matchStatus && matchPriority;
    });
  }, [tasks, debouncedSearch, statusFilter, priorityFilter]);

  const toggleSelect = (id: string) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  };

  const bulkStatusMutation = useMutation({
    mutationFn: async (status: TaskStatus) => {
      const results = await Promise.allSettled(
        [...selectedIds].map((id) => taskApi.changeStatus(id, status))
      );
      const failed = results.filter((r) => r.status === "rejected").length;
      if (failed > 0) throw new Error(`${failed} task(s) failed to update`);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.tasks.all(projectId) });
      setSelectedIds(new Set());
      toast("success", "Tasks updated");
    },
    onError: (err: Error) => toast("error", "Partial failure", err.message),
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: async () => {
      const results = await Promise.allSettled(
        [...selectedIds].map((id) => taskApi.delete(id))
      );
      const failed = results.filter((r) => r.status === "rejected").length;
      if (failed > 0) throw new Error(`${failed} task(s) could not be deleted`);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.tasks.all(projectId) });
      setSelectedIds(new Set());
      toast("success", "Tasks deleted");
    },
    onError: (err: Error) => toast("error", "Partial failure", err.message),
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Tasks"
        workspaceId={workspaceId}
        actions={
          <Button size="sm" onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4" />
            New Task
          </Button>
        }
      />

      {/* Filters */}
      <div className="flex items-center gap-2 px-4 py-2 border-b border-[var(--color-border)] overflow-x-auto shrink-0">
        <div className="relative shrink-0">
          <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-[var(--color-muted-foreground)]" aria-hidden="true" />
          <input
            aria-label="Search tasks"
            className="h-7 w-48 rounded-md border border-[var(--color-input)] bg-transparent pl-8 pr-3 text-xs focus:outline-none focus:ring-1 focus:ring-[var(--color-ring)] placeholder:text-[var(--color-muted-foreground)]"
            placeholder="Search tasks..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-1 overflow-x-auto">
          {STATUS_FILTER.map((s) => (
            <button
              key={s.value}
              onClick={() => setStatusFilter(s.value)}
              className={cn(
                "shrink-0 rounded-md px-2.5 py-1 text-xs font-medium transition-colors",
                statusFilter === s.value
                  ? "bg-[var(--color-primary)] text-white"
                  : "bg-[var(--color-muted)] text-[var(--color-muted-foreground)] hover:bg-[var(--color-accent)]"
              )}
            >
              {s.label}
            </button>
          ))}
        </div>
        {selectedIds.size > 0 && (
          <div className="ml-auto flex items-center gap-2 shrink-0">
            <span className="text-xs text-[var(--color-muted-foreground)]">{selectedIds.size} selected</span>
            <Button size="sm" variant="outline" onClick={() => bulkStatusMutation.mutate("DONE")} loading={bulkStatusMutation.isPending}>
              Mark Done
            </Button>
            <Button size="sm" variant="destructive" onClick={() => bulkDeleteMutation.mutate()} loading={bulkDeleteMutation.isPending}>
              Delete
            </Button>
          </div>
        )}
      </div>

      {/* Table */}
      <div className="flex-1 overflow-y-auto">
        {isLoading ? (
          <div className="p-4">
            <Skeleton className="h-8 w-full mb-2" />
            {[1,2,3,4,5].map(i => <Skeleton key={i} className="h-12 w-full mb-1" />)}
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={ListTodo}
            title="No tasks found"
            description={search ? "Try a different search term" : "Create your first task"}
            action={!search ? { label: "New Task", onClick: () => setShowCreate(true) } : undefined}
            className="h-full"
          />
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-[var(--color-border)] bg-[var(--color-muted)]/40">
                <th className="w-8 px-3 py-2">
                  <input
                    type="checkbox"
                    checked={selectedIds.size === filtered.length && filtered.length > 0}
                    onChange={(e) => {
                      if (e.target.checked) setSelectedIds(new Set(filtered.map(t => t.id)));
                      else setSelectedIds(new Set());
                    }}
                    className="rounded"
                  />
                </th>
                <th className="px-3 py-2 text-left text-xs font-medium text-[var(--color-muted-foreground)]">Title</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-[var(--color-muted-foreground)] hidden md:table-cell">Status</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-[var(--color-muted-foreground)] hidden lg:table-cell">Priority</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-[var(--color-muted-foreground)] hidden lg:table-cell">Type</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-[var(--color-muted-foreground)] hidden xl:table-cell">Due</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-[var(--color-muted-foreground)]">Assignee</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((task) => (
                <tr
                  key={task.id}
                  className={cn(
                    "border-b border-[var(--color-border)] hover:bg-[var(--color-muted)]/30 cursor-pointer transition-colors group",
                    selectedIds.has(task.id) && "bg-[var(--color-primary)]/5"
                  )}
                  onClick={() => setSelectedTaskId(task.id)}
                >
                  <td className="px-3 py-2.5" onClick={(e) => { e.stopPropagation(); toggleSelect(task.id); }}>
                    <input type="checkbox" checked={selectedIds.has(task.id)} onChange={() => {}} className="rounded" />
                  </td>
                  <td className="px-3 py-2.5 min-w-0">
                    <div className="flex items-center gap-2">
                      <PriorityIcon priority={task.priority} className="shrink-0 md:hidden" />
                      <span className="truncate max-w-[280px] font-medium">{task.title}</span>
                      {task.labels?.map((l) => (
                        <span key={l.id} className="hidden xl:inline-block px-1.5 py-0.5 text-[10px] rounded-full text-white font-medium shrink-0" style={{ backgroundColor: l.color }}>
                          {l.name}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-3 py-2.5 hidden md:table-cell"><TaskStatusBadge status={task.status} size="xs" /></td>
                  <td className="px-3 py-2.5 hidden lg:table-cell">
                    <div className="flex items-center gap-1.5">
                      <PriorityIcon priority={task.priority} />
                      <span className="text-xs text-[var(--color-muted-foreground)]">{task.priority.replace("_", " ")}</span>
                    </div>
                  </td>
                  <td className="px-3 py-2.5 hidden lg:table-cell">
                    <Badge variant="outline" className="text-[10px] px-1.5 py-0">{task.type}</Badge>
                  </td>
                  <td className="px-3 py-2.5 hidden xl:table-cell text-xs text-[var(--color-muted-foreground)]">
                    {task.dueDate ? formatDate(task.dueDate) : "—"}
                  </td>
                  <td className="px-3 py-2.5">
                    {task.assignee ? (
                      <Avatar name={task.assignee.displayName ?? task.assignee.email} src={task.assignee.avatarUrl} size="xs" />
                    ) : (
                      <span className="text-xs text-[var(--color-muted-foreground)]">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <CreateTaskModal open={showCreate} onClose={() => setShowCreate(false)} projectId={projectId} />
      <TaskDrawer taskId={selectedTaskId} projectId={projectId} workspaceId={workspaceId} onClose={() => setSelectedTaskId(null)} />
    </div>
  );
}
