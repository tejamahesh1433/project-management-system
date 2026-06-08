"use client";
import { useState, useCallback } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  type DragStartEvent,
  type DragOverEvent,
  type DragEndEvent,
  closestCorners,
} from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
  useSortable,
  arrayMove,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { boardApi, taskApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { useToast } from "@/components/ui/toast";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { TaskStatusBadge, PriorityIcon } from "@/components/ui/task-status";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { CreateTaskModal } from "@/components/tasks/create-task-modal";
import { TaskDrawer } from "@/components/tasks/task-drawer";
import { cn } from "@/lib/utils";
import { Plus, MoreHorizontal, GripVertical, Trash2 } from "lucide-react";
import type { Board, BoardColumn, Task, TaskStatus } from "@/types";

const STATUS_OPTIONS: Array<{ value: TaskStatus; label: string }> = [
  { value: "TODO", label: "Todo" },
  { value: "IN_PROGRESS", label: "In Progress" },
  { value: "IN_REVIEW", label: "In Review" },
  { value: "DONE", label: "Done" },
  { value: "CANCELLED", label: "Cancelled" },
];

interface KanbanBoardProps {
  board: Board;
  projectId: string;
  workspaceId: string;
}

function TaskCard({ task, onClick }: { task: Task; onClick: () => void }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: task.id,
    data: { type: "task", task },
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        "group relative rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] p-3 cursor-pointer hover:shadow-md transition-all select-none",
        isDragging && "opacity-50 shadow-xl rotate-2"
      )}
      onClick={onClick}
    >
      <div
        {...attributes}
        {...listeners}
        className="absolute left-1.5 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity cursor-grab active:cursor-grabbing"
        onClick={(e) => e.stopPropagation()}
      >
        <GripVertical className="h-4 w-4 text-[var(--color-muted-foreground)]" />
      </div>
      <div className="ml-3">
        <p className="text-sm font-medium leading-snug mb-2 line-clamp-2">{task.title}</p>
        <div className="flex items-center gap-2 flex-wrap">
          <PriorityIcon priority={task.priority} />
          <Badge variant="outline" className="text-[10px] px-1.5 py-0">{task.type}</Badge>
          {task.labels?.map((l) => (
            <span key={l.id} className="px-1.5 py-0.5 text-[10px] rounded-full text-white font-medium" style={{ backgroundColor: l.color }}>
              {l.name}
            </span>
          ))}
        </div>
        {(task.assignee || task.dueDate) && (
          <div className="flex items-center justify-between mt-2">
            {task.dueDate && (
              <span className="text-[10px] text-[var(--color-muted-foreground)]">
                {new Date(task.dueDate).toLocaleDateString("en-US", { month: "short", day: "numeric" })}
              </span>
            )}
            {task.assignee && (
              <Avatar name={task.assignee.displayName ?? task.assignee.email} src={task.assignee.avatarUrl} size="xs" className="ml-auto" />
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function KanbanColumn({
  column,
  projectId,
  workspaceId,
  onAddTask,
  onTaskClick,
  onDeleteColumn,
}: {
  column: BoardColumn;
  projectId: string;
  workspaceId: string;
  onAddTask: (columnId: string) => void;
  onTaskClick: (taskId: string) => void;
  onDeleteColumn: (columnId: string) => void;
}) {
  const { setNodeRef } = useSortable({ id: column.id, data: { type: "column" } });

  return (
    <div ref={setNodeRef} className="flex flex-col w-72 shrink-0">
      <div className="flex items-center justify-between mb-3 px-1">
        <div className="flex items-center gap-2">
          <h3 className="text-sm font-semibold">{column.name}</h3>
          <span className="rounded-full bg-[var(--color-muted)] px-2 py-0.5 text-xs font-medium text-[var(--color-muted-foreground)]">
            {column.tasks.length}
          </span>
        </div>
        <div className="flex items-center gap-1">
          <Button variant="ghost" size="icon-sm" onClick={() => onAddTask(column.id)}>
            <Plus className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon-sm" onClick={() => onDeleteColumn(column.id)}>
            <Trash2 className="h-3.5 w-3.5 text-[var(--color-muted-foreground)]" />
          </Button>
        </div>
      </div>
      <div className="flex-1 min-h-0">
        <SortableContext items={column.tasks.map((t) => t.id)} strategy={verticalListSortingStrategy}>
          <div className="space-y-2 min-h-[80px] rounded-lg p-2 bg-[var(--color-muted)]/30 overflow-y-auto max-h-[calc(100vh-280px)]">
            {column.tasks.map((task) => (
              <TaskCard key={task.id} task={task} onClick={() => onTaskClick(task.id)} />
            ))}
            {column.tasks.length === 0 && (
              <div className="flex items-center justify-center h-16 text-xs text-[var(--color-muted-foreground)]">
                Drop tasks here
              </div>
            )}
          </div>
        </SortableContext>
      </div>
    </div>
  );
}

export function KanbanBoard({ board, projectId, workspaceId }: KanbanBoardProps) {
  const qc = useQueryClient();
  const { toast } = useToast();
  const [activeTask, setActiveTask] = useState<Task | null>(null);
  const [selectedTaskId, setSelectedTaskId] = useState<string | null>(null);
  const [addColumnOpen, setAddColumnOpen] = useState(false);
  const [columnName, setColumnName] = useState("");
  const [columnStatus, setColumnStatus] = useState<TaskStatus>("TODO");
  const [createTaskForColumn, setCreateTaskForColumn] = useState<string | null>(null);

  const { data: boardData, isLoading } = useQuery<Board>({
    queryKey: queryKeys.boards.detail(board.id),
    queryFn: () => boardApi.get(board.id),
  });

  const columns = boardData?.columns ?? board.columns ?? [];

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );

  const moveTaskMutation = useMutation({
    mutationFn: ({ taskId, targetColumnId }: { taskId: string; targetColumnId: string }) =>
      boardApi.moveTask(board.id, { taskId, targetColumnId }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.boards.detail(board.id) });
    },
    onError: () => toast("error", "Failed to move task"),
  });

  const createColumnMutation = useMutation({
    mutationFn: () =>
      boardApi.createColumn(board.id, { name: columnName, taskStatus: columnStatus, position: columns.length }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.boards.detail(board.id) });
      setAddColumnOpen(false);
      setColumnName("");
      toast("success", "Column added");
    },
  });

  const deleteColumnMutation = useMutation({
    mutationFn: (columnId: string) => boardApi.deleteColumn(board.id, columnId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.boards.detail(board.id) });
      toast("success", "Column deleted");
    },
  });

  const handleDragStart = (event: DragStartEvent) => {
    const data = event.active.data.current;
    if (data?.type === "task") setActiveTask(data.task);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    setActiveTask(null);
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const activeData = active.data.current;
    const overData = over.data.current;

    if (activeData?.type !== "task") return;

    let targetColumnId: string | null = null;
    if (overData?.type === "column") {
      targetColumnId = over.id as string;
    } else if (overData?.type === "task") {
      const overTask = overData.task as Task;
      const col = columns.find((c) => c.tasks.some((t) => t.id === overTask.id));
      if (col) targetColumnId = col.id;
    }

    if (targetColumnId) {
      moveTaskMutation.mutate({ taskId: active.id as string, targetColumnId });
    }
  };

  if (isLoading) {
    return (
      <div className="flex gap-4 p-6 overflow-x-auto h-full">
        {[1,2,3].map(i => (
          <div key={i} className="w-72 shrink-0 space-y-3">
            <div className="h-8 animate-pulse bg-[var(--color-muted)] rounded" />
            {[1,2,3].map(j => <div key={j} className="h-24 animate-pulse bg-[var(--color-muted)] rounded" />)}
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      <DndContext sensors={sensors} collisionDetection={closestCorners} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
        <div className="flex gap-4 p-4 overflow-x-auto flex-1 items-start">
          <SortableContext items={columns.map((c) => c.id)}>
            {columns.map((col) => (
              <KanbanColumn
                key={col.id}
                column={col}
                projectId={projectId}
                workspaceId={workspaceId}
                onAddTask={() => setCreateTaskForColumn(col.id)}
                onTaskClick={setSelectedTaskId}
                onDeleteColumn={(id) => deleteColumnMutation.mutate(id)}
              />
            ))}
          </SortableContext>

          <button
            onClick={() => setAddColumnOpen(true)}
            className="flex items-center gap-2 w-72 shrink-0 rounded-lg border-2 border-dashed border-[var(--color-border)] p-4 text-sm text-[var(--color-muted-foreground)] hover:border-[var(--color-primary)] hover:text-[var(--color-primary)] transition-colors"
          >
            <Plus className="h-4 w-4" />
            Add column
          </button>
        </div>

        <DragOverlay>
          {activeTask ? (
            <div className="rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] p-3 shadow-2xl rotate-3 w-72">
              <p className="text-sm font-medium">{activeTask.title}</p>
            </div>
          ) : null}
        </DragOverlay>
      </DndContext>

      {/* Add Column Modal */}
      <Modal open={addColumnOpen} onClose={() => setAddColumnOpen(false)} title="Add Column" size="sm">
        <ModalBody className="space-y-4">
          <Input
            label="Column name"
            placeholder="e.g. In Review"
            value={columnName}
            onChange={(e) => setColumnName(e.target.value)}
            autoFocus
          />
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium">Task status</label>
            <select
              value={columnStatus}
              onChange={(e) => setColumnStatus(e.target.value as TaskStatus)}
              className="h-9 w-full rounded-md border border-[var(--color-input)] bg-[var(--color-background)] px-3 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-ring)]"
            >
              {STATUS_OPTIONS.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
            </select>
          </div>
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setAddColumnOpen(false)}>Cancel</Button>
          <Button onClick={() => createColumnMutation.mutate()} loading={createColumnMutation.isPending} disabled={!columnName.trim()}>
            Add Column
          </Button>
        </ModalFooter>
      </Modal>

      <CreateTaskModal
        open={!!createTaskForColumn}
        onClose={() => setCreateTaskForColumn(null)}
        projectId={projectId}
      />

      <TaskDrawer
        taskId={selectedTaskId}
        projectId={projectId}
        workspaceId={workspaceId}
        onClose={() => setSelectedTaskId(null)}
      />
    </div>
  );
}
