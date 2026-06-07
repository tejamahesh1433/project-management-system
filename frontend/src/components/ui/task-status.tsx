import { cn } from "@/lib/utils";
import type { TaskStatus, TaskPriority } from "@/types";

const statusConfig: Record<TaskStatus, { label: string; className: string; dot: string }> = {
  TODO: { label: "Todo", className: "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400", dot: "bg-gray-400" },
  IN_PROGRESS: { label: "In Progress", className: "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400", dot: "bg-blue-500" },
  IN_REVIEW: { label: "In Review", className: "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400", dot: "bg-purple-500" },
  DONE: { label: "Done", className: "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400", dot: "bg-green-500" },
  CANCELLED: { label: "Cancelled", className: "bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400 line-through", dot: "bg-red-400" },
};

const priorityConfig: Record<TaskPriority, { label: string; color: string }> = {
  URGENT: { label: "Urgent", color: "text-red-600" },
  HIGH: { label: "High", color: "text-orange-500" },
  MEDIUM: { label: "Medium", color: "text-yellow-500" },
  LOW: { label: "Low", color: "text-blue-400" },
  NO_PRIORITY: { label: "No Priority", color: "text-[var(--color-muted-foreground)]" },
};

export function TaskStatusBadge({ status, size = "sm" }: { status: TaskStatus; size?: "xs" | "sm" }) {
  const cfg = statusConfig[status];
  return (
    <span className={cn(
      "inline-flex items-center gap-1.5 rounded-full font-medium",
      size === "xs" ? "px-1.5 py-0.5 text-[10px]" : "px-2 py-0.5 text-xs",
      cfg.className
    )}>
      <span className={cn("rounded-full shrink-0", size === "xs" ? "h-1.5 w-1.5" : "h-2 w-2", cfg.dot)} />
      {cfg.label}
    </span>
  );
}

export function PriorityLabel({ priority }: { priority: TaskPriority }) {
  const cfg = priorityConfig[priority];
  return <span className={cn("text-xs font-medium", cfg.color)}>{cfg.label}</span>;
}

export function PriorityIcon({ priority, className }: { priority: TaskPriority; className?: string }) {
  const colors: Record<TaskPriority, string> = {
    URGENT: "text-red-600",
    HIGH: "text-orange-500",
    MEDIUM: "text-yellow-500",
    LOW: "text-blue-400",
    NO_PRIORITY: "text-[var(--color-muted-foreground)]",
  };
  const bars: Record<TaskPriority, number> = {
    URGENT: 4,
    HIGH: 3,
    MEDIUM: 2,
    LOW: 1,
    NO_PRIORITY: 0,
  };
  const filled = bars[priority];

  return (
    <div className={cn("flex items-end gap-px", colors[priority], className)}>
      {[1, 2, 3, 4].map((h) => (
        <div
          key={h}
          className={cn(
            "rounded-sm",
            h <= filled ? "bg-current" : "bg-current opacity-25",
          )}
          style={{ width: 3, height: h * 3 + 2 }}
        />
      ))}
    </div>
  );
}
