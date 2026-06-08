"use client";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { boardApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { KanbanBoard } from "@/components/kanban/kanban-board";
import { Skeleton } from "@/components/ui/skeleton";
import type { Board } from "@/types";

export default function BoardDetailPage() {
  const { workspaceId, projectId, boardId } = useParams<{ workspaceId: string; projectId: string; boardId: string }>();

  const { data: board, isLoading } = useQuery<Board>({
    queryKey: queryKeys.boards.detail(boardId),
    queryFn: () => boardApi.get(boardId),
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title={board?.name ?? "Board"} workspaceId={workspaceId} />
      <div className="flex-1 overflow-hidden">
        {isLoading ? (
          <div className="flex gap-4 p-6 overflow-x-auto">
            {[1,2,3].map(i => (
              <div key={i} className="w-72 shrink-0 space-y-3">
                <Skeleton className="h-8" />
                {[1,2,3].map(j => <Skeleton key={j} className="h-24" />)}
              </div>
            ))}
          </div>
        ) : board ? (
          <KanbanBoard board={board} projectId={projectId} workspaceId={workspaceId} />
        ) : null}
      </div>
    </div>
  );
}
