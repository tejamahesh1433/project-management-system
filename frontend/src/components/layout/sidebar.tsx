"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard, FolderKanban, ListTodo, Zap, FileText, BarChart3,
  Bell, Activity, Settings, LogOut, Bot, Plug, ChevronDown, Plus,
  Home, Users, Globe, Inbox
} from "lucide-react";
import { useAuthStore } from "@/stores/auth";
import { useQuery } from "@tanstack/react-query";
import { workspaceApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Avatar } from "@/components/ui/avatar";
import { useState } from "react";
import type { Workspace } from "@/types";

const navItems = (workspaceId: string) => [
  { href: `/workspaces/${workspaceId}/overview`, icon: Home, label: "Overview" },
  { href: `/workspaces/${workspaceId}/projects`, icon: FolderKanban, label: "Projects" },
  { href: `/workspaces/${workspaceId}/dashboards`, icon: LayoutDashboard, label: "Dashboards" },
  { href: `/workspaces/${workspaceId}/analytics`, icon: BarChart3, label: "Analytics" },
  { href: `/workspaces/${workspaceId}/reports`, icon: FileText, label: "Reports" },
  { href: `/workspaces/${workspaceId}/activity`, icon: Activity, label: "Activity" },
  { href: `/workspaces/${workspaceId}/ai`, icon: Bot, label: "AI Assistant" },
  { href: `/workspaces/${workspaceId}/integrations`, icon: Plug, label: "Integrations" },
  { href: `/workspaces/${workspaceId}/members`, icon: Users, label: "Members" },
];

interface SidebarProps {
  workspaceId: string;
}

export function Sidebar({ workspaceId }: SidebarProps) {
  const pathname = usePathname();
  const user = useAuthStore((s) => s.user);
  const clearAuth = useAuthStore((s) => s.clearAuth);
  const [wsOpen, setWsOpen] = useState(false);

  const { data: workspaces = [] } = useQuery<Workspace[]>({
    queryKey: queryKeys.workspaces.all,
    queryFn: workspaceApi.list,
    enabled: !!user,
  });

  const { data: workspace } = useQuery<Workspace>({
    queryKey: queryKeys.workspaces.detail(workspaceId),
    queryFn: () => workspaceApi.get(workspaceId),
    enabled: !!workspaceId,
  });

  const items = navItems(workspaceId);

  return (
    <aside className="flex h-screen w-56 flex-col border-r border-[var(--color-sidebar-border)] bg-[var(--color-sidebar)] shrink-0">
      {/* Workspace switcher */}
      <div className="relative p-2 border-b border-[var(--color-sidebar-border)]">
        <button
          onClick={() => setWsOpen((p) => !p)}
          className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 hover:bg-[var(--color-accent)] transition-colors"
        >
          <div className="h-6 w-6 rounded bg-[var(--color-primary)] flex items-center justify-center shrink-0">
            <span className="text-white text-[10px] font-bold">
              {workspace?.name?.[0]?.toUpperCase() ?? "W"}
            </span>
          </div>
          <span className="flex-1 text-left text-sm font-semibold truncate">
            {workspace?.name ?? "Loading..."}
          </span>
          <ChevronDown className={cn("h-3.5 w-3.5 text-[var(--color-muted-foreground)] transition-transform", wsOpen && "rotate-180")} />
        </button>

        {wsOpen && (
          <div className="absolute left-2 right-2 top-full mt-1 z-50 rounded-lg border border-[var(--color-border)] bg-[var(--color-popover)] shadow-lg p-1 animate-fade-in">
            {workspaces.map((ws) => (
              <Link
                key={ws.id}
                href={`/workspaces/${ws.id}/projects`}
                onClick={() => setWsOpen(false)}
                className={cn(
                  "flex items-center gap-2 rounded-md px-2 py-1.5 text-sm hover:bg-[var(--color-accent)] transition-colors",
                  ws.id === workspaceId && "bg-[var(--color-accent)] font-medium"
                )}
              >
                <div className="h-5 w-5 rounded bg-[var(--color-primary)] flex items-center justify-center shrink-0">
                  <span className="text-white text-[9px] font-bold">{ws.name[0]?.toUpperCase()}</span>
                </div>
                <span className="truncate">{ws.name}</span>
              </Link>
            ))}
            <div className="my-1 h-px bg-[var(--color-border)]" />
            <Link
              href="/dashboard"
              onClick={() => setWsOpen(false)}
              className="flex items-center gap-2 rounded-md px-2 py-1.5 text-sm hover:bg-[var(--color-accent)] text-[var(--color-muted-foreground)]"
            >
              <Plus className="h-4 w-4" />
              New workspace
            </Link>
          </div>
        )}
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto p-2 space-y-0.5">
        {items.map((item) => {
          const active = pathname.startsWith(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-2.5 rounded-md px-2 py-1.5 text-sm transition-colors",
                active
                  ? "bg-[var(--color-accent)] text-[var(--color-foreground)] font-medium"
                  : "text-[var(--color-sidebar-foreground)] hover:bg-[var(--color-accent)] hover:text-[var(--color-foreground)]"
              )}
            >
              <item.icon className="h-4 w-4 shrink-0" />
              {item.label}
            </Link>
          );
        })}
      </nav>

      {/* User section */}
      <div className="p-2 border-t border-[var(--color-sidebar-border)] space-y-0.5">
        <Link
          href={`/workspaces/${workspaceId}/notifications`}
          className="flex items-center gap-2.5 rounded-md px-2 py-1.5 text-sm text-[var(--color-sidebar-foreground)] hover:bg-[var(--color-accent)] transition-colors"
        >
          <Inbox className="h-4 w-4 shrink-0" />
          Notifications
        </Link>
        <Link
          href={`/workspaces/${workspaceId}/settings`}
          className="flex items-center gap-2.5 rounded-md px-2 py-1.5 text-sm text-[var(--color-sidebar-foreground)] hover:bg-[var(--color-accent)] transition-colors"
        >
          <Settings className="h-4 w-4 shrink-0" />
          Settings
        </Link>
        {user && (
          <div className="flex items-center gap-2 px-2 py-1.5 rounded-md hover:bg-[var(--color-accent)] transition-colors cursor-pointer group" onClick={() => { clearAuth(); window.location.href = "/login"; }}>
            <Avatar name={user.displayName || user.email} src={user.avatarUrl} size="xs" />
            <div className="flex-1 min-w-0">
              <p className="text-xs font-medium truncate">{user.displayName || user.email}</p>
              <p className="text-[10px] text-[var(--color-muted-foreground)] truncate">{user.email}</p>
            </div>
            <LogOut className="h-3.5 w-3.5 text-[var(--color-muted-foreground)] opacity-0 group-hover:opacity-100 transition-opacity" />
          </div>
        )}
      </div>
    </aside>
  );
}
