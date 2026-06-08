"use client";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { activityApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Avatar } from "@/components/ui/avatar";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { formatRelativeTime } from "@/lib/utils";
import { Activity } from "lucide-react";
import type { Activity as ActivityType } from "@/types";

export default function WorkspaceActivityPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();

  const { data: activities = [], isLoading } = useQuery<ActivityType[]>({
    queryKey: queryKeys.activity.workspace(workspaceId),
    queryFn: () => activityApi.workspace(workspaceId),
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Workspace Activity" workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-2xl mx-auto">
          {isLoading ? (
            <div className="space-y-4">
              {[1,2,3,4,5].map(i => (
                <div key={i} className="flex gap-3 items-start">
                  <Skeleton className="h-8 w-8 rounded-full" />
                  <div className="flex-1 space-y-1.5">
                    <Skeleton className="h-4 w-3/4" />
                    <Skeleton className="h-3 w-1/3" />
                  </div>
                </div>
              ))}
            </div>
          ) : activities.length === 0 ? (
            <EmptyState icon={Activity} title="No activity yet" description="Activity from across your workspace will appear here." />
          ) : (
            <div className="relative">
              <div className="absolute left-4 top-0 bottom-0 w-px bg-[var(--color-border)]" />
              <div className="space-y-4">
                {activities.map((a) => (
                  <div key={a.id} className="flex items-start gap-3 relative pl-10">
                    <div className="absolute left-0 flex h-8 w-8 items-center justify-center rounded-full bg-[var(--color-card)] border border-[var(--color-border)]">
                      <Avatar name={a.actor?.displayName ?? "?"} src={a.actor?.avatarUrl} size="xs" />
                    </div>
                    <div className="flex-1 min-w-0 pt-1">
                      <p className="text-sm">
                        <span className="font-medium">{a.actor?.displayName}</span>{" "}
                        <span className="text-[var(--color-muted-foreground)]">{a.action?.toLowerCase().replace(/_/g, " ")}</span>{" "}
                        <span className="font-medium">{a.entityType?.toLowerCase()}</span>
                      </p>
                      <p className="text-xs text-[var(--color-muted-foreground)] mt-0.5">{formatRelativeTime(a.createdAt)}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
