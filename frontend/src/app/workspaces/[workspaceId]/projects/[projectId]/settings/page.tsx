"use client";
import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { projectApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { useToast } from "@/components/ui/toast";
import { Trash2, AlertTriangle } from "lucide-react";

const PROJECT_COLORS = ["#6366f1","#22c55e","#f59e0b","#ef4444","#8b5cf6","#06b6d4","#ec4899","#84cc16"];

export default function ProjectSettingsPage() {
  const { workspaceId, projectId } = useParams<{ workspaceId: string; projectId: string }>();
  const router = useRouter();
  const qc = useQueryClient();
  const { toast } = useToast();

  const [form, setForm] = useState({ name: "", description: "", color: PROJECT_COLORS[0] });
  const [showDelete, setShowDelete] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState("");

  const { data: project, isLoading } = useQuery({
    queryKey: queryKeys.projects.detail(projectId),
    queryFn: () => projectApi.get(projectId),
  });

  useEffect(() => {
    if (project) {
      setForm({ name: project.name, description: project.description ?? "", color: project.color ?? PROJECT_COLORS[0] });
    }
  }, [project]);

  const updateMutation = useMutation({
    mutationFn: () => {
      const slug = form.name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)+/g, "") || "project";
      return projectApi.update(projectId, { ...form, slug, status: project?.status ?? "ACTIVE" });
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.projects.detail(projectId) });
      toast("success", "Project updated");
    },
    onError: () => toast("error", "Failed to update project"),
  });

  const deleteMutation = useMutation({
    mutationFn: () => projectApi.delete(projectId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.projects.all(workspaceId) });
      toast("success", "Project deleted");
      router.push(`/workspaces/${workspaceId}/projects`);
    },
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="Project Settings" workspaceId={workspaceId} />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-2xl mx-auto space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>General</CardTitle>
              <CardDescription>Update your project information</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Input
                label="Project name"
                value={form.name}
                onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))}
              />
              <Textarea
                label="Description"
                value={form.description}
                onChange={(e) => setForm(f => ({ ...f, description: e.target.value }))}
                className="h-20"
              />
              <div>
                <label className="text-sm font-medium block mb-2">Color</label>
                <div className="flex items-center gap-2 flex-wrap">
                  {PROJECT_COLORS.map((c) => (
                    <button
                      key={c}
                      onClick={() => setForm(f => ({ ...f, color: c }))}
                      className="h-7 w-7 rounded-full transition-transform hover:scale-110"
                      style={{ backgroundColor: c, outline: form.color === c ? `2px solid ${c}` : "none", outlineOffset: 2 }}
                    />
                  ))}
                </div>
              </div>
              <div className="flex justify-end pt-2">
                <Button onClick={() => updateMutation.mutate()} loading={updateMutation.isPending}>
                  Save Changes
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card className="border-[var(--color-destructive)]/20">
            <CardHeader>
              <CardTitle className="text-[var(--color-destructive)]">Danger Zone</CardTitle>
              <CardDescription>Irreversible actions for this project</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">Delete project</p>
                  <p className="text-xs text-[var(--color-muted-foreground)]">Permanently remove this project and all its data</p>
                </div>
                <Button variant="destructive" size="sm" onClick={() => setShowDelete(true)}>
                  <Trash2 className="h-4 w-4" />
                  Delete
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Delete project" size="sm">
        <ModalBody className="space-y-4">
          <div className="flex items-center gap-3 p-3 rounded-lg bg-[var(--color-destructive)]/10">
            <AlertTriangle className="h-5 w-5 text-[var(--color-destructive)] shrink-0" />
            <p className="text-sm text-[var(--color-destructive)]">This action cannot be undone. All tasks, boards, and documents will be lost.</p>
          </div>
          <Input
            label={`Type "${project?.name}" to confirm`}
            placeholder={project?.name}
            value={deleteConfirm}
            onChange={(e) => setDeleteConfirm(e.target.value)}
          />
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowDelete(false)}>Cancel</Button>
          <Button
            variant="destructive"
            onClick={() => deleteMutation.mutate()}
            loading={deleteMutation.isPending}
            disabled={deleteConfirm !== project?.name}
          >
            Delete Project
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
