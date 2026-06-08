"use client";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { analyticsApi, taskApi, projectApi, activityApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { TaskStatusBadge } from "@/components/ui/task-status";
import { Avatar } from "@/components/ui/avatar";
import { formatRelativeTime } from "@/lib/utils";
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, Tooltip, Cell, CartesianGrid, PieChart, Pie } from "recharts";
import type { Project, Task, Activity } from "@/types";

const COLORS = ["#6366f1", "#22c55e", "#f59e0b", "#ef4444", "#8b5cf6"];

export default function DashboardsPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();

  const { data: analytics, isLoading } = useQuery({
    queryKey: queryKeys.analytics.workspace(workspaceId),
    queryFn: () => analyticsApi.workspace(workspaceId),
  });

  const { data: projects = [] } = useQuery<Project[]>({
    queryKey: queryKeys.projects.all(workspaceId),
    queryFn: () => projectApi.list(workspaceId),
  });

  const { data: activities = [] } = useQuery<Activity[]>({
    queryKey: queryKeys.activity.workspace(workspaceId),
    queryFn: () => activityApi.workspace(workspaceId),
  });

  const statusData = analytics?.tasksByStatus
    ? Object.entries(analytics.tasksByStatus).map(([name, value]) => ({ name: name.replace("_", " "), value: Number(value) }))
    : [];

  const priorityData = analytics?.tasksByPriority
    ? Object.entries(analytics.tasksByPriority).map(([name, value]) => ({ name: name.replace("_", " "), value: Number(value) }))
    : [];

  const completionPct = analytics?.completionRate ? Math.round(Number(analytics.completionRate)) : 0;

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Dashboard" workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-6xl mx-auto space-y-6">
          {/* KPIs */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {isLoading ? [1,2,3,4].map(i => <Skeleton key={i} className="h-24" />) : [
              { label: "Total Tasks", value: analytics?.totalTasks ?? 0, accent: "#6366f1" },
              { label: "Completed", value: analytics?.completedTasks ?? 0, accent: "#22c55e" },
              { label: "In Progress", value: analytics?.inProgressTasks ?? 0, accent: "#f59e0b" },
              { label: "Completion", value: `${completionPct}%`, accent: "#8b5cf6" },
            ].map((kpi) => (
              <Card key={kpi.label}>
                <CardContent className="pt-5">
                  <div className="h-1 w-8 rounded-full mb-3" style={{ backgroundColor: kpi.accent }} />
                  <p className="text-2xl font-bold">{kpi.value}</p>
                  <p className="text-xs text-[var(--color-muted-foreground)] mt-0.5">{kpi.label}</p>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Status chart */}
            <Card className="lg:col-span-2">
              <CardHeader><CardTitle>Task Status Distribution</CardTitle></CardHeader>
              <CardContent>
                {isLoading ? <Skeleton className="h-48" /> : (
                  <ResponsiveContainer width="100%" height={200}>
                    <BarChart data={statusData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                      <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                      <YAxis tick={{ fontSize: 11 }} />
                      <Tooltip />
                      <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                        {statusData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>

            {/* Priority chart */}
            <Card>
              <CardHeader><CardTitle>By Priority</CardTitle></CardHeader>
              <CardContent>
                {isLoading ? <Skeleton className="h-48" /> : (
                  <ResponsiveContainer width="100%" height={200}>
                    <PieChart>
                      <Pie data={priorityData} cx="50%" cy="50%" outerRadius={70} dataKey="value" label={({ name }) => name} labelLine={false}>
                        {priorityData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Recent activity */}
            <Card>
              <CardHeader><CardTitle>Recent Activity</CardTitle></CardHeader>
              <CardContent className="pt-0">
                {activities.slice(0, 6).map((a) => (
                  <div key={a.id} className="flex items-start gap-2.5 py-2.5 border-b border-[var(--color-border)] last:border-0">
                    <Avatar name={a.actor?.displayName ?? "?"} src={a.actor?.avatarUrl} size="xs" className="mt-0.5" />
                    <div className="flex-1 min-w-0">
                      <p className="text-xs">
                        <span className="font-medium">{a.actor?.displayName}</span>{" "}
                        <span className="text-[var(--color-muted-foreground)]">{a.action?.toLowerCase().replace(/_/g, " ")}</span>{" "}
                        <span className="font-medium">{a.entityType?.toLowerCase()}</span>
                      </p>
                      <p className="text-[10px] text-[var(--color-muted-foreground)] mt-0.5">{formatRelativeTime(a.createdAt)}</p>
                    </div>
                  </div>
                ))}
                {activities.length === 0 && <p className="text-sm text-[var(--color-muted-foreground)] py-4 text-center">No activity yet</p>}
              </CardContent>
            </Card>

            {/* Projects summary */}
            <Card>
              <CardHeader><CardTitle>Projects ({projects.length})</CardTitle></CardHeader>
              <CardContent className="pt-0">
                {projects.slice(0, 5).map((p) => (
                  <div key={p.id} className="flex items-center gap-3 py-2.5 border-b border-[var(--color-border)] last:border-0">
                    <div className="h-2 w-2 rounded-full shrink-0" style={{ backgroundColor: p.color ?? "#6366f1" }} />
                    <span className="flex-1 text-sm font-medium truncate">{p.name}</span>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${p.status === "ACTIVE" ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-600"}`}>
                      {p.status}
                    </span>
                  </div>
                ))}
                {projects.length === 0 && <p className="text-sm text-[var(--color-muted-foreground)] py-4 text-center">No projects</p>}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
