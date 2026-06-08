"use client";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { sprintApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, Tooltip, Cell, CartesianGrid, PieChart, Pie } from "recharts";
import type { SprintMetrics } from "@/types";

const COLORS = ["#22c55e","#6366f1","#f59e0b","#ef4444","#8b5cf6"];

export default function SprintMetricsPage() {
  const { workspaceId, projectId, sprintId } = useParams<{ workspaceId: string; projectId: string; sprintId: string }>();

  const { data: metrics, isLoading } = useQuery<SprintMetrics>({
    queryKey: queryKeys.sprints.metrics(sprintId),
    queryFn: () => sprintApi.metrics(sprintId),
  });

  const statusData = metrics ? [
    { name: "Done", value: metrics.completedTasks },
    { name: "In Progress", value: metrics.inProgressTasks },
    { name: "Todo", value: metrics.todoTasks },
  ] : [];

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Sprint Metrics" workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {isLoading ? [1,2,3,4].map(i => <Skeleton key={i} className="h-24" />) : [
              { label: "Total Tasks", value: metrics?.totalTasks ?? 0 },
              { label: "Completed", value: metrics?.completedTasks ?? 0 },
              { label: "In Progress", value: metrics?.inProgressTasks ?? 0 },
              { label: "Completion Rate", value: metrics?.completionRate ? `${Math.round(metrics.completionRate)}%` : "0%" },
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
              <CardHeader><CardTitle>Task Status</CardTitle></CardHeader>
              <CardContent>
                {isLoading ? <Skeleton className="h-48" /> : (
                  <ResponsiveContainer width="100%" height={200}>
                    <PieChart>
                      <Pie data={statusData} cx="50%" cy="50%" outerRadius={80} dataKey="value" label={({ name, value }) => `${name}: ${value}`}>
                        {statusData.map((_, i) => <Cell key={i} fill={COLORS[i]} />)}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader><CardTitle>Progress</CardTitle></CardHeader>
              <CardContent>
                {isLoading ? <Skeleton className="h-48" /> : (
                  <div className="space-y-4 pt-4">
                    <div>
                      <div className="flex justify-between text-sm mb-2">
                        <span>Completion</span>
                        <span className="font-semibold">{metrics?.completionRate ? `${Math.round(metrics.completionRate)}%` : "0%"}</span>
                      </div>
                      <div className="h-3 rounded-full bg-[var(--color-muted)] overflow-hidden">
                        <div className="h-full rounded-full bg-green-500 transition-all" style={{ width: `${metrics?.completionRate ?? 0}%` }} />
                      </div>
                    </div>
                    {metrics?.totalEstimatedHours && (
                      <div>
                        <div className="flex justify-between text-sm mb-2">
                          <span>Hours</span>
                          <span className="font-semibold">{metrics.completedEstimatedHours}/{metrics.totalEstimatedHours}h</span>
                        </div>
                        <div className="h-3 rounded-full bg-[var(--color-muted)] overflow-hidden">
                          <div
                            className="h-full rounded-full bg-[var(--color-primary)] transition-all"
                            style={{ width: `${Math.min(((metrics.completedEstimatedHours ?? 0) / metrics.totalEstimatedHours) * 100, 100)}%` }}
                          />
                        </div>
                      </div>
                    )}
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
