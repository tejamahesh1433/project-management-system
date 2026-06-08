"use client";
import { Bell } from "lucide-react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { notificationApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/ui/theme-toggle";
import type { UnreadCount } from "@/types";
import { cn } from "@/lib/utils";
import type { ReactNode } from "react";

interface HeaderProps {
  title?: string;
  workspaceId: string;
  actions?: ReactNode;
}

export function Header({ title, workspaceId, actions }: HeaderProps) {
  const { data: unread } = useQuery<UnreadCount>({
    queryKey: queryKeys.notifications.unread,
    queryFn: notificationApi.unread,
    refetchInterval: 30000,
  });

  return (
    <header className="flex h-12 shrink-0 items-center gap-3 border-b border-[var(--color-border)] px-4">
      {title && (
        <h1 className="text-sm font-semibold text-[var(--color-foreground)] truncate">{title}</h1>
      )}
      <div className="flex-1" />
      <div className="flex items-center gap-1">
        {actions}
        <ThemeToggle />
        <Link href={`/workspaces/${workspaceId}/notifications`} aria-label={`Notifications${unread?.count ? ` (${unread.count} unread)` : ""}`}>
          <Button variant="ghost" size="icon-sm" className="relative" aria-label="Open notifications">
            <Bell className="h-4 w-4" aria-hidden="true" />
            {unread?.count ? (
              <span
                aria-hidden="true"
                className={cn(
                  "absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-[var(--color-primary)] text-[10px] font-medium text-white",
                  unread.count > 9 && "w-auto px-1"
                )}
              >
                {unread.count > 9 ? "9+" : unread.count}
              </span>
            ) : null}
          </Button>
        </Link>
      </div>
    </header>
  );
}
