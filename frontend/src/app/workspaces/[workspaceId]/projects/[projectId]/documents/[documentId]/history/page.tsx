"use client";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { documentApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Avatar } from "@/components/ui/avatar";
import { useToast } from "@/components/ui/toast";
import { formatRelativeTime } from "@/lib/utils";
import { RotateCcw, Clock } from "lucide-react";
import type { DocumentVersion } from "@/types";

export default function DocumentHistoryPage() {
  const { workspaceId, projectId, documentId } = useParams<{ workspaceId: string; projectId: string; documentId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();

  const { data: versions = [], isLoading } = useQuery<DocumentVersion[]>({
    queryKey: queryKeys.documents.versions(documentId),
    queryFn: () => documentApi.getVersions(documentId),
  });

  const restoreMutation = useMutation({
    mutationFn: (versionNumber: number) => documentApi.restoreVersion(documentId, versionNumber),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.documents.detail(documentId) });
      toast("success", "Version restored");
      window.history.back();
    },
    onError: () => toast("error", "Restore failed"),
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Version History" workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-2xl mx-auto">
          {isLoading ? (
            <div className="space-y-3">
              {[1,2,3].map(i => <div key={i} className="h-16 animate-pulse bg-[var(--color-muted)] rounded-lg" />)}
            </div>
          ) : versions.length === 0 ? (
            <div className="text-center py-12">
              <Clock className="h-10 w-10 text-[var(--color-muted-foreground)] mx-auto mb-3" />
              <p className="text-sm text-[var(--color-muted-foreground)]">No version history yet</p>
            </div>
          ) : (
            <div className="space-y-2">
              {versions.map((v, i) => (
                <div key={v.id} className="flex items-center gap-3 rounded-lg border border-[var(--color-border)] p-4 bg-[var(--color-card)]">
                  <div className="h-8 w-8 rounded-full bg-[var(--color-muted)] flex items-center justify-center text-xs font-bold shrink-0">
                    v{v.versionNumber}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium">Version {v.versionNumber}</p>
                    <div className="flex items-center gap-2 text-xs text-[var(--color-muted-foreground)]">
                      {v.author && <Avatar name={v.author.displayName ?? v.author.email} src={v.author.avatarUrl} size="xs" />}
                      <span>{formatRelativeTime(v.createdAt)}</span>
                    </div>
                  </div>
                  {i > 0 && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => restoreMutation.mutate(v.versionNumber)}
                      loading={restoreMutation.isPending}
                    >
                      <RotateCcw className="h-3.5 w-3.5" />
                      Restore
                    </Button>
                  )}
                  {i === 0 && (
                    <span className="text-xs text-[var(--color-primary)] font-medium px-2 py-1 bg-[var(--color-primary)]/10 rounded-full">Current</span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
