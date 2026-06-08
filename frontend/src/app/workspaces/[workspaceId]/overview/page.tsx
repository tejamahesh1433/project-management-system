"use client";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { workspaceApi, projectApi, activityApi, notificationApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { formatRelativeTime } from "@/lib/utils";
import { FolderKanban, Users, Bell, Activity, Plus } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import type { Project, Activity as ActivityType, Notification } from "@/types";

export default function WorkspaceOverviewPage() {
  const params = useParams<{ workspaceId: string }>();
  const workspaceId = params.workspaceId;

  const { data: workspace, isLoading: wsLoading } = useQuery({
    queryKey: queryKeys.workspaces.detail(workspaceId),
    queryFn: () => workspaceApi.get(workspaceId),
  });

  const { data: projects = [], isLoading: projectsLoading } = useQuery<Project[]>({
    queryKey: queryKeys.projects.all(workspaceId),
    queryFn: () => projectApi.list(workspaceId),
  });

  const { data: members = [], isLoading: membersLoading } = useQuery({
    queryKey: queryKeys.workspaces.members(workspaceId),
    queryFn: () => workspaceApi.listMembers(workspaceId),
  });

  const { data: activities = [], isLoading: activitiesLoading } = useQuery<ActivityType[]>({
    queryKey: queryKeys.activity.workspace(workspaceId),
    queryFn: () => activityApi.workspace(workspaceId),
  });

  const { data: notifications = [], isLoading: notificationsLoading } = useQuery<Notification[]>({
    queryKey: queryKeys.notifications.all,
    queryFn: notificationApi.list,
  });

  const unread = notifications.filter((n) => !n.read).length;
  const recentProjects = projects.slice(0, 6);
  const recentActivities = activities.slice(0, 8);

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title={workspace?.name ?? "Workspace"}
        workspaceId={workspaceId}
        actions={
          <Link href={`/workspaces/${workspaceId}/projects`}>
            <Button size="sm">
              <Plus className="h-4 w-4" />
              New Project
            </Button>
          </Link>
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-5xl mx-auto space-y-6">
          {/* Stats */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {[
              { icon: FolderKanban, label: "Projects", value: projects.length, color: "text-blue-500", loading: projectsLoading },
              { icon: Users, label: "Members", value: members.length, color: "text-purple-500", loading: membersLoading },
              { icon: Activity, label: "Activities", value: activities.length, color: "text-green-500", loading: activitiesLoading },
              { icon: Bell, label: "Unread", value: unread, color: "text-amber-500", loading: notificationsLoading },
            ].map((stat) => (
              <Card key={stat.label}>
                <CardContent className="pt-6">
                  <div className="flex items-center gap-3">
                    <div className="rounded-lg bg-[var(--color-muted)] p-2.5">
                      <stat.icon className={`h-5 w-5 ${stat.color}`} />
                    </div>
                    <div>
                      {stat.loading ? <Skeleton className="h-8 w-12 mb-1" /> : <p className="text-2xl font-bold">{stat.value}</p>}
                      <p className="text-xs text-[var(--color-muted-foreground)]">{stat.label}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Recent Projects */}
            <div className="lg:col-span-2">
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle>Recent Projects</CardTitle>
                    <Link href={`/workspaces/${workspaceId}/projects`}>
                      <Button variant="ghost" size="sm">View all</Button>
                    </Link>
                  </div>
                </CardHeader>
                <CardContent className="pt-0">
                  {projectsLoading ? (
                    <div className="space-y-3">
                      {[1,2,3].map(i => <Skeleton key={i} className="h-14" />)}
                    </div>
                  ) : recentProjects.length === 0 ? (
                    <div className="text-center py-8 text-sm text-[var(--color-muted-foreground)]">
                      No projects yet.{" "}
                      <Link href={`/workspaces/${workspaceId}/projects`} className="text-[var(--color-primary)] hover:underline">
                        Create one
                      </Link>
                    </div>
                  ) : (
                    <div className="divide-y divide-[var(--color-border)]">
                      {recentProjects.map((p) => (
                        <Link
                          key={p.id}
                          href={`/workspaces/${workspaceId}/projects/${p.id}/overview`}
                          className="flex items-center gap-3 py-3 hover:bg-[var(--color-muted)]/50 -mx-2 px-2 rounded-md transition-colors"
                        >
                          <div className="h-8 w-8 rounded-md flex items-center justify-center shrink-0" style={{ backgroundColor: p.color ?? "#6366f1" }}>
                            <FolderKanban className="h-4 w-4 text-white" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium truncate">{p.name}</p>
                            {p.description && <p className="text-xs text-[var(--color-muted-foreground)] truncate">{p.description}</p>}
                          </div>
                          <Badge variant={p.status === "ACTIVE" ? "success" : "secondary"}>
                            {p.status}
                          </Badge>
                        </Link>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>

            {/* Recent Activity */}
            <Card>
              <CardHeader>
                <CardTitle>Activity</CardTitle>
              </CardHeader>
              <CardContent className="pt-0">
                {activitiesLoading ? (
                  <div className="space-y-3">
                    {[1, 2, 3].map((i) => <Skeleton key={i} className="h-10" />)}
                  </div>
                ) : recentActivities.length === 0 ? (
                  <p className="text-sm text-[var(--color-muted-foreground)] text-center py-6">No activity yet</p>
                ) : (
                  <div className="space-y-3">
                    {recentActivities.map((a) => (
                      <div key={a.id} className="flex items-start gap-2.5">
                        <Avatar name={a.actor?.displayName ?? "?"} src={a.actor?.avatarUrl} size="xs" className="mt-0.5" />
                        <div className="flex-1 min-w-0">
                          <p className="text-xs text-[var(--color-foreground)]">
                            <span className="font-medium">{a.actor?.displayName}</span>{" "}
                            <span className="text-[var(--color-muted-foreground)]">{a.action?.toLowerCase().replace(/_/g, " ")}</span>{" "}
                            <span className="font-medium">{a.entityType?.toLowerCase()}</span>
                          </p>
                          <p className="text-[10px] text-[var(--color-muted-foreground)] mt-0.5">{formatRelativeTime(a.createdAt)}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
