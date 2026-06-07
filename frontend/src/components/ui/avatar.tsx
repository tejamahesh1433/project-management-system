import { cn, getInitials, generateColor } from "@/lib/utils";
import type { HTMLAttributes } from "react";

interface AvatarProps extends HTMLAttributes<HTMLDivElement> {
  name: string;
  src?: string;
  size?: "xs" | "sm" | "md" | "lg";
}

const sizeMap = {
  xs: "h-6 w-6 text-xs",
  sm: "h-8 w-8 text-xs",
  md: "h-9 w-9 text-sm",
  lg: "h-12 w-12 text-base",
};

export function Avatar({ name, src, size = "sm", className, ...props }: AvatarProps) {
  const initials = getInitials(name);
  const colorClass = generateColor(name);

  return (
    <div
      className={cn(
        "relative inline-flex shrink-0 items-center justify-center rounded-full font-medium text-white overflow-hidden",
        sizeMap[size],
        !src && colorClass,
        className
      )}
      title={name}
      {...props}
    >
      {src ? (
        <img src={src} alt={name} className="h-full w-full object-cover" />
      ) : (
        <span>{initials}</span>
      )}
    </div>
  );
}

export function AvatarGroup({ users, max = 3, size = "sm" }: {
  users: Array<{ name: string; src?: string }>;
  max?: number;
  size?: "xs" | "sm" | "md";
}) {
  const visible = users.slice(0, max);
  const remaining = users.length - max;

  const sizeClass = {
    xs: "h-6 w-6 text-xs",
    sm: "h-8 w-8 text-xs",
    md: "h-9 w-9 text-sm",
  }[size];

  return (
    <div className="flex -space-x-2">
      {visible.map((u, i) => (
        <div key={i} className="ring-2 ring-[var(--color-background)] rounded-full">
          <Avatar name={u.name} src={u.src} size={size} />
        </div>
      ))}
      {remaining > 0 && (
        <div className={cn(
          "ring-2 ring-[var(--color-background)] rounded-full inline-flex items-center justify-center bg-[var(--color-muted)] text-[var(--color-muted-foreground)] font-medium",
          sizeClass
        )}>
          +{remaining}
        </div>
      )}
    </div>
  );
}
