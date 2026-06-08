"use client";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { notificationApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { cn, formatRelativeTime } from "@/lib/utils";
import { Bell, CheckCheck, Trash2, BellOff } from "lucide-react";
import type { Notification } from "@/types";

export default function NotificationsPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const qc = useQueryClient();

  const { data: notifications = [], isLoading } = useQuery<Notification[]>({
    queryKey: queryKeys.notifications.all,
    queryFn: notificationApi.list,
  });

  const markReadMutation = useMutation({
    mutationFn: (id: string) => notificationApi.markRead(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.notifications.all });
      qc.invalidateQueries({ queryKey: queryKeys.notifications.unread });
    },
  });

  const markAllMutation = useMutation({
    mutationFn: notificationApi.markAllRead,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.notifications.all });
      qc.invalidateQueries({ queryKey: queryKeys.notifications.unread });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => notificationApi.delete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.notifications.all });
      qc.invalidateQueries({ queryKey: queryKeys.notifications.unread });
    },
  });

  const unread = notifications.filter((n) => !n.read);

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Notifications"
        workspaceId={workspaceId}
        actions={
          unread.length > 0 ? (
            <Button size="sm" variant="outline" onClick={() => markAllMutation.mutate()} loading={markAllMutation.isPending}>
              <CheckCheck className="h-4 w-4" />
              Mark all read
            </Button>
          ) : undefined
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-2xl mx-auto">
          {isLoading ? (
            <div className="space-y-2">
              {[1,2,3,4,5].map(i => <div key={i} className="h-20 animate-pulse bg-[var(--color-muted)] rounded-lg" />)}
            </div>
          ) : notifications.length === 0 ? (
            <EmptyState
              icon={BellOff}
              title="No notifications"
              description="You're all caught up!"
            />
          ) : (
            <div className="space-y-1">
              {unread.length > 0 && (
                <p className="text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider mb-3">
                  Unread ({unread.length})
                </p>
              )}
              {notifications.map((n) => (
                <div
                  key={n.id}
                  className={cn(
                    "group flex items-start gap-3 rounded-lg p-3 transition-colors cursor-pointer",
                    !n.read ? "bg-[var(--color-primary)]/5 border border-[var(--color-primary)]/10" : "hover:bg-[var(--color-muted)]/50"
                  )}
                  onClick={() => { if (!n.read) markReadMutation.mutate(n.id); }}
                >
                  <div className={cn(
                    "mt-0.5 h-2 w-2 rounded-full shrink-0",
                    !n.read ? "bg-[var(--color-primary)]" : "bg-transparent"
                  )} />
                  <div className="flex-1 min-w-0">
                    <p className={cn("text-sm", !n.read && "font-semibold")}>{n.title}</p>
                    <p className="text-xs text-[var(--color-muted-foreground)] mt-0.5">{n.message}</p>
                    <p className="text-[10px] text-[var(--color-muted-foreground)] mt-1">{formatRelativeTime(n.createdAt)}</p>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    className="opacity-0 group-hover:opacity-100 shrink-0"
                    onClick={(e) => { e.stopPropagation(); deleteMutation.mutate(n.id); }}
                  >
                    <Trash2 className="h-3.5 w-3.5 text-[var(--color-muted-foreground)]" />
                  </Button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
