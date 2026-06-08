"use client";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { workspaceApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { useAuthStore } from "@/stores/auth";
import type { Workspace } from "@/types";

export default function DashboardPage() {
  const router = useRouter();
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const hasHydrated = useAuthStore((s) => s._hasHydrated);

  const { data: workspaces } = useQuery<Workspace[]>({
    queryKey: queryKeys.workspaces.all,
    queryFn: workspaceApi.list,
    enabled: isAuthenticated && hasHydrated,
  });

  useEffect(() => {
    // Wait for store to rehydrate from localStorage before making redirect decisions
    if (!hasHydrated) return;
    if (!isAuthenticated) {
      router.replace("/login");
      return;
    }
    if (workspaces === undefined) return;
    const first = workspaces[0];
    if (first) {
      router.replace(`/workspaces/${first.id}/overview`);
    } else {
      router.replace("/workspaces/new");
    }
  }, [isAuthenticated, hasHydrated, workspaces, router]);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="h-8 w-8 rounded-full border-2 border-[var(--color-primary)] border-t-transparent animate-spin" />
    </div>
  );
}
