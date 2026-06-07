"use client";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { projectApi, taskApi, sprintApi, boardApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarGroup } from "@/components/ui/avatar";
import { TaskStatusBadge } from "@/components/ui/task-status";
import { Skeleton } from "@/components/ui/skeleton";
import { formatDate } from "@/lib/utils";
import { ListTodo, GitBranch, LayoutGrid, Calendar, CheckCircle } from "lucide-react";
import Link from "next/link";
import type { Task, Sprint } from "@/types";

export default function ProjectOverviewPage() {
  const { workspaceId, projectId } = useParams<{ workspaceId: string; projectId: string }>();

  const { data: project, isLoading: projectLoading } = useQuery({
    queryKey: queryKeys.projects.detail(projectId),
    queryFn: () => projectApi.get(projectId),
  });

  const { data: tasks = [] } = useQuery<Task[]>({
    queryKey: queryKeys.tasks.all(projectId),
    queryFn: () => taskApi.list(projectId),
  });

  const { data: sprints = [] } = useQuery<Sprint[]>({
    queryKey: queryKeys.sprints.all(projectId),
    queryFn: () => sprintApi.list(projectId),
  });

  const { data: boards = [] } = useQuery({
    queryKey: queryKeys.boards.all(projectId),
    queryFn: () => boardApi.list(projectId),
  });

  const { data: members = [] } = useQuery({
    queryKey: queryKeys.projects.members(projectId),
    queryFn: () => projectApi.listMembers(projectId),
  });

  const done = tasks.filter((t) => t.status === "DONE").length;
  const inProgress = tasks.filter((t) => t.status === "IN_PROGRESS").length;
  const todo = tasks.filter((t) => t.status === "TODO").length;
  const completionPct = tasks.length > 0 ? Math.round((done / tasks.length) * 100) : 0;
  const activeSprint = sprints.find((s) => s.status === "ACTIVE");

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title={project?.name ?? "Project"} workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto space-y-6">
          {projectLoading ? <Skeleton className="h-24" /> : project && (
            <Card>
              <CardContent className="pt-6">
                <div className="flex items-start gap-4">
                  <div className="h-12 w-12 rounded-xl flex items-center justify-center shrink-0" style={{ backgroundColor: project.color ?? "#6366f1" }}>
                    <span className="text-white font-bold text-lg">{project.name[0]}</span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h1 className="text-xl font-bold">{project.name}</h1>
                      <Badge variant={project.status === "ACTIVE" ? "success" : "secondary"}>{project.status}</Badge>
                    </div>
                    {project.description && <p className="text-sm text-[var(--color-muted-foreground)] mt-1">{project.description}</p>}
                    <div className="flex items-center gap-4 mt-3 text-xs text-[var(--color-muted-foreground)]">
                      <span className="flex items-center gap-1"><Calendar className="h-3.5 w-3.5" />Created {formatDate(project.createdAt)}</span>
                      {members.length > 0 && (
                        <AvatarGroup users={members.map((m: { user?: { firstName?: string; avatarUrl?: string } }) => ({ name: m.user?.firstName ?? "?", src: m.user?.avatarUrl }))} size="xs" />
                      )}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {[
              { icon: ListTodo, label: "Total Tasks", value: tasks.length, color: "text-blue-500", href: "tasks" },
              { icon: CheckCircle, label: "Completed", value: done, color: "text-green-500", href: "tasks" },
              { icon: GitBranch, label: "Sprints", value: sprints.length, color: "text-purple-500", href: "sprints" },
              { icon: LayoutGrid, label: "Boards", value: boards.length, color: "text-amber-500", href: "boards" },
            ].map((stat) => (
              <Link key={stat.label} href={`/workspaces/${workspaceId}/projects/${projectId}/${stat.href}`}>
                <Card className="hover:shadow-md transition-shadow cursor-pointer">
                  <CardContent className="pt-5">
                    <div className="flex items-center gap-3">
                      <div className="rounded-lg bg-[var(--color-muted)] p-2">
                        <stat.icon className={`h-4 w-4 ${stat.color}`} />
                      </div>
                      <div>
                        <p className="text-xl font-bold">{stat.value}</p>
                        <p className="text-xs text-[var(--color-muted-foreground)]">{stat.label}</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader><CardTitle>Task Progress</CardTitle></CardHeader>
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-[var(--color-muted-foreground)]">Overall completion</span>
                    <span className="font-semibold">{completionPct}%</span>
                  </div>
                  <div className="h-2 rounded-full bg-[var(--color-muted)] overflow-hidden">
                    <div className="h-full rounded-full bg-[var(--color-primary)] transition-all" style={{ width: `${completionPct}%` }} />
                  </div>
                  <div className="flex gap-4 text-xs text-[var(--color-muted-foreground)]">
                    <span className="flex items-center gap-1"><span className="h-2 w-2 rounded-full bg-gray-400" />{todo} Todo</span>
                    <span className="flex items-center gap-1"><span className="h-2 w-2 rounded-full bg-blue-500" />{inProgress} In Progress</span>
                    <span className="flex items-center gap-1"><span className="h-2 w-2 rounded-full bg-green-500" />{done} Done</span>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader><CardTitle>Active Sprint</CardTitle></CardHeader>
              <CardContent className="pt-0">
                {activeSprint ? (
                  <div>
                    <p className="font-medium">{activeSprint.name}</p>
                    {activeSprint.goal && <p className="text-sm text-[var(--color-muted-foreground)] mt-1">{activeSprint.goal}</p>}
                    <div className="flex gap-3 mt-3 text-xs text-[var(--color-muted-foreground)]">
                      {activeSprint.startDate && <span>Start: {formatDate(activeSprint.startDate)}</span>}
                      {activeSprint.endDate && <span>End: {formatDate(activeSprint.endDate)}</span>}
                    </div>
                    <Link href={`/workspaces/${workspaceId}/projects/${projectId}/sprints/${activeSprint.id}/overview`}>
                      <button className="mt-3 text-xs text-[var(--color-primary)] hover:underline">View sprint →</button>
                    </Link>
                  </div>
                ) : (
                  <div className="text-sm text-[var(--color-muted-foreground)]">
                    No active sprint.{" "}
                    <Link href={`/workspaces/${workspaceId}/projects/${projectId}/sprints`} className="text-[var(--color-primary)] hover:underline">Start one</Link>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Recent Tasks</CardTitle>
                <Link href={`/workspaces/${workspaceId}/projects/${projectId}/tasks`}>
                  <button className="text-xs text-[var(--color-primary)] hover:underline">View all</button>
                </Link>
              </div>
            </CardHeader>
            <CardContent className="pt-0">
              {tasks.slice(0, 5).map((t) => (
                <div key={t.id} className="flex items-center gap-3 py-2.5 border-b border-[var(--color-border)] last:border-0">
                  <TaskStatusBadge status={t.status} size="xs" />
                  <span className="flex-1 text-sm truncate">{t.title}</span>
                  {t.assignee && <Avatar name={`${t.assignee.firstName} ${t.assignee.lastName}`} src={t.assignee.avatarUrl} size="xs" />}
                </div>
              ))}
              {tasks.length === 0 && <p className="text-sm text-[var(--color-muted-foreground)] py-4 text-center">No tasks yet</p>}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
