import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";
import { Button } from "./button";

interface EmptyStateProps {
  icon?: LucideIcon;
  title: string;
  description?: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  className?: string;
}

export function EmptyState({ icon: Icon, title, description, action, className }: EmptyStateProps) {
  return (
    <div className={cn("flex flex-col items-center justify-center py-16 text-center", className)}>
      {Icon && (
        <div className="mb-4 rounded-full bg-[var(--color-muted)] p-4">
          <Icon className="h-8 w-8 text-[var(--color-muted-foreground)]" />
        </div>
      )}
      <h3 className="text-base font-semibold text-[var(--color-foreground)]">{title}</h3>
      {description && (
        <p className="mt-1.5 text-sm text-[var(--color-muted-foreground)] max-w-xs">{description}</p>
      )}
      {action && (
        <div className="mt-4">
          <Button onClick={action.onClick}>{action.label}</Button>
        </div>
      )}
    </div>
  );
}
