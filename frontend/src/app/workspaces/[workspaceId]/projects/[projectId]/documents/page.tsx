"use client";
import { useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { documentApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { EmptyState } from "@/components/ui/empty-state";
import { Dropdown } from "@/components/ui/dropdown";
import { useToast } from "@/components/ui/toast";
import { formatRelativeTime } from "@/lib/utils";
import { FileText, Plus, MoreHorizontal, Trash2, Edit, Clock, FolderOpen } from "lucide-react";
import type { Document, Folder } from "@/types";

export default function ProjectDocumentsPage() {
  const { workspaceId, projectId } = useParams<{ workspaceId: string; projectId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();

  const [showCreate, setShowCreate] = useState(false);
  const [showCreateFolder, setShowCreateFolder] = useState(false);
  const [newDocTitle, setNewDocTitle] = useState("");
  const [newFolderName, setNewFolderName] = useState("");

  const { data: documents = [], isLoading } = useQuery<Document[]>({
    queryKey: queryKeys.documents.all(projectId),
    queryFn: () => documentApi.listDocuments(projectId),
  });

  const { data: folders = [] } = useQuery<Folder[]>({
    queryKey: queryKeys.documents.folders(projectId),
    queryFn: () => documentApi.listFolders(projectId),
  });

  const createDocMutation = useMutation({
    mutationFn: () => documentApi.createDocument({ projectId, title: newDocTitle }),
    onSuccess: (doc) => {
      qc.invalidateQueries({ queryKey: queryKeys.documents.all(projectId) });
      toast("success", "Document created");
      setShowCreate(false);
      setNewDocTitle("");
      window.location.href = `/workspaces/${workspaceId}/projects/${projectId}/documents/${doc.id}/edit`;
    },
    onError: () => toast("error", "Failed to create document"),
  });

  const createFolderMutation = useMutation({
    mutationFn: () => documentApi.createFolder({ projectId, name: newFolderName }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.documents.folders(projectId) });
      toast("success", "Folder created");
      setShowCreateFolder(false);
      setNewFolderName("");
    },
  });

  const deleteDocMutation = useMutation({
    mutationFn: (id: string) => documentApi.deleteDocument(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.documents.all(projectId) });
      toast("success", "Document deleted");
    },
  });

  const deleteFolderMutation = useMutation({
    mutationFn: (id: string) => documentApi.deleteFolder(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.documents.folders(projectId) });
      toast("success", "Folder deleted");
    },
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Documents"
        workspaceId={workspaceId}
        actions={
          <div className="flex items-center gap-2">
            <Button size="sm" variant="outline" onClick={() => setShowCreateFolder(true)}>
              <FolderOpen className="h-4 w-4" />
              New Folder
            </Button>
            <Button size="sm" onClick={() => setShowCreate(true)}>
              <Plus className="h-4 w-4" />
              New Document
            </Button>
          </div>
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Folders */}
          {folders.length > 0 && (
            <div>
              <h2 className="text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider mb-3">Folders</h2>
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                {folders.map((f) => (
                  <div key={f.id} className="group relative flex items-center gap-2.5 rounded-lg border border-[var(--color-border)] p-3 hover:shadow-sm transition-shadow cursor-pointer">
                    <FolderOpen className="h-5 w-5 text-amber-500 shrink-0" />
                    <span className="text-sm font-medium truncate flex-1">{f.name}</span>
                    <Dropdown
                      trigger={
                        <Button variant="ghost" size="icon-sm" className="opacity-0 group-hover:opacity-100 shrink-0">
                          <MoreHorizontal className="h-3.5 w-3.5" />
                        </Button>
                      }
                      align="right"
                      items={[
                        { label: "Delete", icon: <Trash2 className="h-4 w-4" />, destructive: true, onClick: () => deleteFolderMutation.mutate(f.id) },
                      ]}
                    />
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Documents */}
          <div>
            {folders.length > 0 && (
              <h2 className="text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider mb-3">Documents</h2>
            )}
            {isLoading ? (
              <div className="space-y-2">
                {[1,2,3].map(i => <div key={i} className="h-16 animate-pulse bg-[var(--color-muted)] rounded-lg" />)}
              </div>
            ) : documents.length === 0 ? (
              <EmptyState
                icon={FileText}
                title="No documents yet"
                description="Create your first document to start collaborating"
                action={{ label: "New Document", onClick: () => setShowCreate(true) }}
              />
            ) : (
              <div className="space-y-2">
                {documents.map((doc) => (
                  <div key={doc.id} className="group flex items-center gap-3 rounded-lg border border-[var(--color-border)] p-3 hover:shadow-sm transition-shadow">
                    <FileText className="h-5 w-5 text-[var(--color-primary)] shrink-0" />
                    <div className="flex-1 min-w-0">
                      <Link href={`/workspaces/${workspaceId}/projects/${projectId}/documents/${doc.id}/edit`} className="text-sm font-medium hover:text-[var(--color-primary)] hover:underline truncate block">
                        {doc.title}
                      </Link>
                      <div className="flex items-center gap-2 mt-0.5 text-[10px] text-[var(--color-muted-foreground)]">
                        <Clock className="h-3 w-3" />
                        Updated {formatRelativeTime(doc.updatedAt)}
                        <span>v{doc.currentVersion}</span>
                      </div>
                    </div>
                    <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity shrink-0">
                      <Link href={`/workspaces/${workspaceId}/projects/${projectId}/documents/${doc.id}/edit`}>
                        <Button variant="ghost" size="icon-sm">
                          <Edit className="h-3.5 w-3.5" />
                        </Button>
                      </Link>
                      <Link href={`/workspaces/${workspaceId}/projects/${projectId}/documents/${doc.id}/history`}>
                        <Button variant="ghost" size="icon-sm">
                          <Clock className="h-3.5 w-3.5" />
                        </Button>
                      </Link>
                      <Button variant="ghost" size="icon-sm" onClick={() => deleteDocMutation.mutate(doc.id)}>
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

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Document" size="sm">
        <ModalBody>
          <Input label="Document title" placeholder="Untitled" value={newDocTitle} onChange={(e) => setNewDocTitle(e.target.value)} autoFocus onKeyDown={(e) => { if (e.key === "Enter" && newDocTitle.trim()) createDocMutation.mutate(); }} />
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowCreate(false)}>Cancel</Button>
          <Button onClick={() => createDocMutation.mutate()} loading={createDocMutation.isPending} disabled={!newDocTitle.trim()}>Create</Button>
        </ModalFooter>
      </Modal>

      <Modal open={showCreateFolder} onClose={() => setShowCreateFolder(false)} title="New Folder" size="sm">
        <ModalBody>
          <Input label="Folder name" placeholder="My Folder" value={newFolderName} onChange={(e) => setNewFolderName(e.target.value)} autoFocus onKeyDown={(e) => { if (e.key === "Enter" && newFolderName.trim()) createFolderMutation.mutate(); }} />
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowCreateFolder(false)}>Cancel</Button>
          <Button onClick={() => createFolderMutation.mutate()} loading={createFolderMutation.isPending} disabled={!newFolderName.trim()}>Create</Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
