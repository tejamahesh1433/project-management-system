"use client";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { analyticsApi, projectApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Select } from "@/components/ui/select";
import { useState } from "react";
import {
  PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend,
  LineChart, Line, CartesianGrid
} from "recharts";
import type { Project } from "@/types";

const COLORS = ["#6366f1", "#22c55e", "#f59e0b", "#ef4444", "#8b5cf6", "#06b6d4"];

export default function WorkspaceAnalyticsPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const [projectFilter, setProjectFilter] = useState("all");

  const { data: analytics, isLoading } = useQuery({
    queryKey: queryKeys.analytics.workspace(workspaceId),
    queryFn: () => analyticsApi.workspace(workspaceId),
  });

  const { data: projects = [] } = useQuery<Project[]>({
    queryKey: queryKeys.projects.all(workspaceId),
    queryFn: () => projectApi.list(workspaceId),
  });

  const statusData = analytics?.tasksByStatus
    ? Object.entries(analytics.tasksByStatus).map(([name, value]) => ({ name: name.replace("_", " "), value }))
    : [];

  const priorityData = analytics?.tasksByPriority
    ? Object.entries(analytics.tasksByPriority).map(([name, value]) => ({ name: name.replace("_", " "), value }))
    : [];

  const typeData = analytics?.tasksByType
    ? Object.entries(analytics.tasksByType).map(([name, value]) => ({ name, value }))
    : [];

  const kpis = [
    { label: "Total Tasks", value: analytics?.totalTasks ?? 0 },
    { label: "Completed", value: analytics?.completedTasks ?? 0 },
    { label: "In Progress", value: analytics?.inProgressTasks ?? 0 },
    { label: "Completion Rate", value: analytics?.completionRate ? `${Math.round(analytics.completionRate)}%` : "0%" },
  ];

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Analytics" workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-5xl mx-auto space-y-6">
          {/* KPI Cards */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {isLoading ? (
              [1,2,3,4].map(i => <Skeleton key={i} className="h-24" />)
            ) : (
              kpis.map((kpi) => (
                <Card key={kpi.label}>
                  <CardContent className="pt-6">
                    <p className="text-3xl font-bold text-[var(--color-primary)]">{kpi.value}</p>
                    <p className="text-sm text-[var(--color-muted-foreground)] mt-1">{kpi.label}</p>
                  </CardContent>
                </Card>
              ))
            )}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Status Chart */}
            <Card>
              <CardHeader><CardTitle>Tasks by Status</CardTitle></CardHeader>
              <CardContent>
                {isLoading ? <Skeleton className="h-56" /> : (
                  <ResponsiveContainer width="100%" height={220}>
                    <PieChart>
                      <Pie data={statusData} cx="50%" cy="50%" innerRadius={55} outerRadius={85} dataKey="value" label={({ name, value }) => `${name}: ${value}`} labelLine={false}>
                        {statusData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>

            {/* Priority Chart */}
            <Card>
              <CardHeader><CardTitle>Tasks by Priority</CardTitle></CardHeader>
              <CardContent>
                {isLoading ? <Skeleton className="h-56" /> : (
                  <ResponsiveContainer width="100%" height={220}>
                    <BarChart data={priorityData} layout="vertical">
                      <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                      <XAxis type="number" tick={{ fontSize: 11 }} />
                      <YAxis type="category" dataKey="name" width={80} tick={{ fontSize: 11 }} />
                      <Tooltip />
                      <Bar dataKey="value" fill="#6366f1" radius={[0, 4, 4, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>

            {/* Type Chart */}
            <Card className="lg:col-span-2">
              <CardHeader><CardTitle>Tasks by Type</CardTitle></CardHeader>
              <CardContent>
                {isLoading ? <Skeleton className="h-56" /> : (
                  <ResponsiveContainer width="100%" height={220}>
                    <BarChart data={typeData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                      <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                      <YAxis tick={{ fontSize: 11 }} />
                      <Tooltip />
                      <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                        {typeData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
