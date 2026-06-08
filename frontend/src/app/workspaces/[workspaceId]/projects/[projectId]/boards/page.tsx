"use client";
import { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { boardApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { Input } from "@/components/ui/input";
import { useToast } from "@/components/ui/toast";
import { KanbanBoard } from "@/components/kanban/kanban-board";
import { LayoutGrid, Plus, ChevronLeft, ChevronRight } from "lucide-react";
import type { Board } from "@/types";

export default function ProjectBoardsPage() {
  const { workspaceId, projectId } = useParams<{ workspaceId: string; projectId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();

  const [showCreate, setShowCreate] = useState(false);
  const [boardName, setBoardName] = useState("");
  const [activeIdx, setActiveIdx] = useState(0);

  const { data: boards = [], isLoading } = useQuery<Board[]>({
    queryKey: queryKeys.boards.all(projectId),
    queryFn: () => boardApi.list(projectId),
  });

  const createMutation = useMutation({
    mutationFn: () => boardApi.create({ projectId, name: boardName, template: "KANBAN" }),
    onSuccess: (board) => {
      qc.invalidateQueries({ queryKey: queryKeys.boards.all(projectId) });
      toast("success", "Board created");
      setShowCreate(false);
      setBoardName("");
      setActiveIdx(boards.length);
    },
    onError: () => toast("error", "Failed to create board"),
  });

  const activeBoard = boards[activeIdx];

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Boards"
        workspaceId={workspaceId}
        actions={
          <Button size="sm" onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4" />
            New Board
          </Button>
        }
      />

      {/* Board tabs */}
      {boards.length > 0 && (
        <div className="flex items-center gap-1 px-4 py-2 border-b border-[var(--color-border)] overflow-x-auto shrink-0">
          {boards.map((b, i) => (
            <button
              key={b.id}
              onClick={() => setActiveIdx(i)}
              className={`shrink-0 px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                i === activeIdx
                  ? "bg-[var(--color-primary)] text-white"
                  : "text-[var(--color-muted-foreground)] hover:bg-[var(--color-accent)]"
              }`}
            >
              {b.name}
            </button>
          ))}
        </div>
      )}

      <div className="flex-1 overflow-hidden">
        {isLoading ? (
          <div className="flex gap-4 p-6 overflow-x-auto h-full">
            {[1,2,3].map(i => (
              <div key={i} className="w-72 shrink-0 space-y-3">
                <Skeleton className="h-8" />
                {[1,2,3].map(j => <Skeleton key={j} className="h-24" />)}
              </div>
            ))}
          </div>
        ) : boards.length === 0 ? (
          <EmptyState
            icon={LayoutGrid}
            title="No boards yet"
            description="Create your first Kanban board to visualize work"
            action={{ label: "Create Board", onClick: () => setShowCreate(true) }}
            className="h-full"
          />
        ) : activeBoard ? (
          <KanbanBoard board={activeBoard} projectId={projectId} workspaceId={workspaceId} />
        ) : null}
      </div>

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Board" size="sm">
        <ModalBody>
          <Input
            label="Board name"
            placeholder="e.g. Sprint Board"
            value={boardName}
            onChange={(e) => setBoardName(e.target.value)}
            autoFocus
            onKeyDown={(e) => { if (e.key === "Enter" && boardName.trim()) createMutation.mutate(); }}
          />
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowCreate(false)}>Cancel</Button>
          <Button onClick={() => createMutation.mutate()} loading={createMutation.isPending} disabled={!boardName.trim()}>
            Create
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
