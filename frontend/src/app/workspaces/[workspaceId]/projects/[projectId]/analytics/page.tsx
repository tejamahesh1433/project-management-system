"use client";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { analyticsApi, taskApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, Tooltip, Cell, CartesianGrid, PieChart, Pie } from "recharts";
import type { Task } from "@/types";

const COLORS = ["#6366f1","#22c55e","#f59e0b","#ef4444","#8b5cf6","#06b6d4"];

export default function ProjectAnalyticsPage() {
  const { workspaceId, projectId } = useParams<{ workspaceId: string; projectId: string }>();

  const { data: analytics, isLoading } = useQuery({
    queryKey: queryKeys.analytics.project(projectId),
    queryFn: () => analyticsApi.project(projectId),
  });

  const { data: tasks = [] } = useQuery<Task[]>({
    queryKey: queryKeys.tasks.all(projectId),
    queryFn: () => taskApi.list(projectId),
  });

  const statusData = analytics?.tasksByStatus
    ? Object.entries(analytics.tasksByStatus).map(([name, value]) => ({ name: name.replace("_", " "), value: Number(value) }))
    : [];

  const priorityData = analytics?.tasksByPriority
    ? Object.entries(analytics.tasksByPriority).map(([name, value]) => ({ name: name.replace("_", " "), value: Number(value) }))
    : [];

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Analytics" workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {isLoading ? [1,2,3,4].map(i => <Skeleton key={i} className="h-24" />) : [
              { label: "Total Tasks", value: analytics?.totalTasks ?? tasks.length },
              { label: "Completed", value: analytics?.completedTasks ?? 0 },
              { label: "In Progress", value: analytics?.inProgressTasks ?? 0 },
              { label: "Completion Rate", value: analytics?.completionRate ? `${Math.round(analytics.completionRate)}%` : "0%" },
            ].map((kpi) => (
              <Card key={kpi.label}>
                <CardContent className="pt-5">
                  <p className="text-2xl font-bold text-[var(--color-primary)]">{kpi.value}</p>
                  <p className="text-xs text-[var(--color-muted-foreground)] mt-1">{kpi.label}</p>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader><CardTitle>Tasks by Status</CardTitle></CardHeader>
              <CardContent>
                {isLoading ? <Skeleton className="h-48" /> : statusData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={200}>
                    <PieChart>
                      <Pie data={statusData} cx="50%" cy="50%" outerRadius={80} dataKey="value" label={({ name }) => name} labelLine={false}>
                        {statusData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                ) : <p className="text-sm text-[var(--color-muted-foreground)] text-center py-8">No data yet</p>}
              </CardContent>
            </Card>

            <Card>
              <CardHeader><CardTitle>Tasks by Priority</CardTitle></CardHeader>
              <CardContent>
                {isLoading ? <Skeleton className="h-48" /> : priorityData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={200}>
                    <BarChart data={priorityData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                      <XAxis dataKey="name" tick={{ fontSize: 10 }} />
                      <YAxis tick={{ fontSize: 10 }} />
                      <Tooltip />
                      <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                        {priorityData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                ) : <p className="text-sm text-[var(--color-muted-foreground)] text-center py-8">No data yet</p>}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
