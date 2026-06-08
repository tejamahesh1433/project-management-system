"use client";
import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { workspaceApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { useToast } from "@/components/ui/toast";
import { useAuthStore } from "@/stores/auth";
import { AlertTriangle, Trash2 } from "lucide-react";

export default function WorkspaceSettingsPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const router = useRouter();
  const qc = useQueryClient();
  const { toast } = useToast();
  const clearAuth = useAuthStore((s) => s.clearAuth);

  const [form, setForm] = useState({ name: "", description: "" });
  const [showDelete, setShowDelete] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState("");

  const { data: workspace } = useQuery({
    queryKey: queryKeys.workspaces.detail(workspaceId),
    queryFn: () => workspaceApi.get(workspaceId),
  });

  useEffect(() => {
    if (workspace) {
      setForm({ name: workspace.name, description: workspace.description ?? "" });
    }
  }, [workspace]);

  const updateMutation = useMutation({
    mutationFn: () => workspaceApi.update(workspaceId, form),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.workspaces.detail(workspaceId) });
      toast("success", "Workspace updated");
    },
    onError: () => toast("error", "Failed to update"),
  });

  const deleteMutation = useMutation({
    mutationFn: () => workspaceApi.delete(workspaceId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.workspaces.all });
      clearAuth();
      router.push("/login");
      toast("success", "Workspace deleted");
    },
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Workspace Settings" workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-2xl mx-auto space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>General</CardTitle>
              <CardDescription>Update workspace information</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Input label="Workspace name" value={form.name} onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))} />
              <Textarea label="Description" value={form.description} onChange={(e) => setForm(f => ({ ...f, description: e.target.value }))} className="h-20" />
              <div className="flex justify-end">
                <Button onClick={() => updateMutation.mutate()} loading={updateMutation.isPending}>Save Changes</Button>
              </div>
            </CardContent>
          </Card>

          <Card className="border-[var(--color-destructive)]/20">
            <CardHeader>
              <CardTitle className="text-[var(--color-destructive)]">Danger Zone</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">Delete workspace</p>
                  <p className="text-xs text-[var(--color-muted-foreground)]">Permanently delete this workspace and all data</p>
                </div>
                <Button variant="destructive" size="sm" onClick={() => setShowDelete(true)}>
                  <Trash2 className="h-4 w-4" />Delete
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Delete workspace" size="sm">
        <ModalBody className="space-y-4">
          <div className="flex items-center gap-3 p-3 rounded-lg bg-[var(--color-destructive)]/10">
            <AlertTriangle className="h-5 w-5 text-[var(--color-destructive)] shrink-0" />
            <p className="text-sm text-[var(--color-destructive)]">This cannot be undone. All projects, tasks, and data will be lost.</p>
          </div>
          <Input
            label={`Type "${workspace?.name}" to confirm`}
            placeholder={workspace?.name}
            value={deleteConfirm}
            onChange={(e) => setDeleteConfirm(e.target.value)}
          />
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowDelete(false)}>Cancel</Button>
          <Button variant="destructive" onClick={() => deleteMutation.mutate()} loading={deleteMutation.isPending} disabled={deleteConfirm !== workspace?.name}>
            Delete Workspace
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
