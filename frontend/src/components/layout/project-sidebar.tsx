"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import {
  LayoutGrid, ListTodo, GitBranch, FileText, FolderOpen, Users, Settings,
  Activity, BarChart3, ChevronLeft, File
} from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { projectApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import type { Project } from "@/types";
import { Button } from "@/components/ui/button";

const projectNav = (workspaceId: string, projectId: string) => [
  { href: `/workspaces/${workspaceId}/projects/${projectId}/overview`, icon: LayoutGrid, label: "Overview" },
  { href: `/workspaces/${workspaceId}/projects/${projectId}/tasks`, icon: ListTodo, label: "Tasks" },
  { href: `/workspaces/${workspaceId}/projects/${projectId}/boards`, icon: LayoutGrid, label: "Board" },
  { href: `/workspaces/${workspaceId}/projects/${projectId}/sprints`, icon: GitBranch, label: "Sprints" },
  { href: `/workspaces/${workspaceId}/projects/${projectId}/documents`, icon: FileText, label: "Documents" },
  { href: `/workspaces/${workspaceId}/projects/${projectId}/files`, icon: File, label: "Files" },
  { href: `/workspaces/${workspaceId}/projects/${projectId}/analytics`, icon: BarChart3, label: "Analytics" },
  { href: `/workspaces/${workspaceId}/projects/${projectId}/activity`, icon: Activity, label: "Activity" },
  { href: `/workspaces/${workspaceId}/projects/${projectId}/members`, icon: Users, label: "Members" },
  { href: `/workspaces/${workspaceId}/projects/${projectId}/settings`, icon: Settings, label: "Settings" },
];

interface ProjectSidebarProps {
  workspaceId: string;
  projectId: string;
}

export function ProjectSidebar({ workspaceId, projectId }: ProjectSidebarProps) {
  const pathname = usePathname();

  const { data: project } = useQuery<Project>({
    queryKey: queryKeys.projects.detail(projectId),
    queryFn: () => projectApi.get(projectId),
  });

  const items = projectNav(workspaceId, projectId);

  return (
    <aside className="flex h-screen w-48 flex-col border-r border-[var(--color-sidebar-border)] bg-[var(--color-sidebar)] shrink-0">
      <div className="p-2 border-b border-[var(--color-sidebar-border)]">
        <Link href={`/workspaces/${workspaceId}/projects`}>
          <Button variant="ghost" size="sm" className="w-full justify-start gap-2 text-[var(--color-muted-foreground)]">
            <ChevronLeft className="h-3.5 w-3.5" />
            Projects
          </Button>
        </Link>
        <div className="px-2 pt-2">
          {project?.color && (
            <div className="h-1.5 w-6 rounded-full mb-2" style={{ backgroundColor: project.color }} />
          )}
          <p className="text-sm font-semibold truncate">{project?.name ?? "..."}</p>
          {project?.description && (
            <p className="text-xs text-[var(--color-muted-foreground)] mt-0.5 line-clamp-2">{project.description}</p>
          )}
        </div>
      </div>

      <nav className="flex-1 overflow-y-auto p-2 space-y-0.5">
        {items.map((item) => {
          const active = pathname === item.href || pathname.startsWith(item.href + "/");
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-2.5 rounded-md px-2 py-1.5 text-sm transition-colors",
                active
                  ? "bg-[var(--color-accent)] text-[var(--color-foreground)] font-medium"
                  : "text-[var(--color-sidebar-foreground)] hover:bg-[var(--color-accent)]"
              )}
            >
              <item.icon className="h-4 w-4 shrink-0" />
              {item.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
