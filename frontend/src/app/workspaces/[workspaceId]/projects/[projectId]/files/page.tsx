"use client";
import { useRef, useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fileApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/components/ui/toast";
import { formatDate } from "@/lib/utils";
import { File, Upload, Trash2, Download } from "lucide-react";

function formatSize(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function ProjectFilesPage() {
  const { workspaceId, projectId } = useParams<{ workspaceId: string; projectId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  const { data: files = [], isLoading } = useQuery({
    queryKey: queryKeys.files.all(projectId),
    queryFn: () => fileApi.list(projectId),
  });

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      await fileApi.upload(projectId, file);
      qc.invalidateQueries({ queryKey: queryKeys.files.all(projectId) });
      toast("success", "File uploaded");
    } catch {
      toast("error", "Upload failed");
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const deleteMutation = useMutation({
    mutationFn: (id: string) => fileApi.delete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.files.all(projectId) });
      toast("success", "File deleted");
    },
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Files"
        workspaceId={workspaceId}
        actions={
          <>
            <input ref={fileInputRef} type="file" className="hidden" onChange={handleUpload} />
            <Button size="sm" onClick={() => fileInputRef.current?.click()} loading={uploading}>
              <Upload className="h-4 w-4" />
              Upload File
            </Button>
          </>
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto">
          {isLoading ? (
            <div className="space-y-2">
              {[1,2,3].map(i => <Skeleton key={i} className="h-14" />)}
            </div>
          ) : files.length === 0 ? (
            <EmptyState
              icon={File}
              title="No files uploaded"
              description="Upload files to share with your team"
              action={{ label: "Upload File", onClick: () => fileInputRef.current?.click() }}
            />
          ) : (
            <div className="divide-y divide-[var(--color-border)] rounded-lg border border-[var(--color-border)] overflow-hidden">
              {files.map((f: { id: string; originalName: string; mimeType: string; size: number; url: string; createdAt: string }) => (
                <div key={f.id} className="group flex items-center gap-3 px-4 py-3 bg-[var(--color-card)] hover:bg-[var(--color-muted)]/30 transition-colors">
                  <div className="h-9 w-9 rounded-lg bg-[var(--color-muted)] flex items-center justify-center shrink-0">
                    <File className="h-5 w-5 text-[var(--color-muted-foreground)]" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{f.originalName}</p>
                    <p className="text-xs text-[var(--color-muted-foreground)]">
                      {formatSize(f.size)} · {f.mimeType} · {formatDate(f.createdAt)}
                    </p>
                  </div>
                  <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <a href={f.url} download={f.originalName}>
                      <Button variant="ghost" size="icon-sm">
                        <Download className="h-3.5 w-3.5" />
                      </Button>
                    </a>
                    <Button variant="ghost" size="icon-sm" onClick={() => deleteMutation.mutate(f.id)}>
                      <Trash2 className="h-3.5 w-3.5 text-[var(--color-destructive)]" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
