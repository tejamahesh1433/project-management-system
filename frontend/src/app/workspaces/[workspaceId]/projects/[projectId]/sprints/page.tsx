"use client";
import { useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { sprintApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { EmptyState } from "@/components/ui/empty-state";
import { Dropdown } from "@/components/ui/dropdown";
import { useToast } from "@/components/ui/toast";
import { formatDate } from "@/lib/utils";
import { GitBranch, Plus, MoreHorizontal, Play, CheckCircle, XCircle, BarChart3 } from "lucide-react";
import type { Sprint, SprintStatus } from "@/types";

const STATUS_CONFIG: Record<SprintStatus, { label: string; variant: "default" | "success" | "warning" | "secondary" | "destructive" | "info" | "outline" }> = {
  PLANNED: { label: "Planned", variant: "secondary" },
  ACTIVE: { label: "Active", variant: "success" },
  COMPLETED: { label: "Completed", variant: "info" },
  CANCELLED: { label: "Cancelled", variant: "destructive" },
};

export default function ProjectSprintsPage() {
  const { workspaceId, projectId } = useParams<{ workspaceId: string; projectId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();

  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ name: "", goal: "", startDate: "", endDate: "" });

  const { data: sprints = [], isLoading } = useQuery<Sprint[]>({
    queryKey: queryKeys.sprints.all(projectId),
    queryFn: () => sprintApi.list(projectId),
  });

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  const createMutation = useMutation({
    mutationFn: () => sprintApi.create({ projectId, ...form }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.sprints.all(projectId) });
      toast("success", "Sprint created");
      setShowCreate(false);
      setForm({ name: "", goal: "", startDate: "", endDate: "" });
    },
    onError: () => toast("error", "Failed to create sprint"),
  });

  const startMutation = useMutation({
    mutationFn: (id: string) => sprintApi.start(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.sprints.all(projectId) });
      toast("success", "Sprint started");
    },
  });

  const completeMutation = useMutation({
    mutationFn: (id: string) => sprintApi.complete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.sprints.all(projectId) });
      toast("success", "Sprint completed");
    },
  });

  const cancelMutation = useMutation({
    mutationFn: (id: string) => sprintApi.cancel(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.sprints.all(projectId) });
      toast("success", "Sprint cancelled");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => sprintApi.delete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.sprints.all(projectId) });
      toast("success", "Sprint deleted");
    },
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Sprints"
        workspaceId={workspaceId}
        actions={
          <Button size="sm" onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4" />
            New Sprint
          </Button>
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-3xl mx-auto">
          {isLoading ? (
            <div className="space-y-3">
              {[1,2,3].map(i => <div key={i} className="h-32 animate-pulse bg-[var(--color-muted)] rounded-lg" />)}
            </div>
          ) : sprints.length === 0 ? (
            <EmptyState
              icon={GitBranch}
              title="No sprints yet"
              description="Organize your work into time-boxed sprints"
              action={{ label: "Create Sprint", onClick: () => setShowCreate(true) }}
            />
          ) : (
            <div className="space-y-3">
              {sprints.map((s) => {
                const cfg = STATUS_CONFIG[s.status];
                return (
                  <Card key={s.id} className="hover:shadow-md transition-shadow">
                    <CardContent className="pt-5">
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap mb-1">
                            <h3 className="font-semibold">{s.name}</h3>
                            <Badge variant={cfg.variant}>{cfg.label}</Badge>
                          </div>
                          {s.goal && <p className="text-sm text-[var(--color-muted-foreground)] mb-2">{s.goal}</p>}
                          {(s.startDate || s.endDate) && (
                            <div className="flex items-center gap-3 text-xs text-[var(--color-muted-foreground)]">
                              {s.startDate && <span>Start: {formatDate(s.startDate)}</span>}
                              {s.endDate && <span>End: {formatDate(s.endDate)}</span>}
                            </div>
                          )}
                        </div>
                        <div className="flex items-center gap-2 shrink-0">
                          {s.status === "PLANNED" && (
                            <Button size="sm" onClick={() => startMutation.mutate(s.id)} loading={startMutation.isPending}>
                              <Play className="h-3.5 w-3.5" />
                              Start
                            </Button>
                          )}
                          {s.status === "ACTIVE" && (
                            <Button size="sm" variant="secondary" onClick={() => completeMutation.mutate(s.id)} loading={completeMutation.isPending}>
                              <CheckCircle className="h-3.5 w-3.5" />
                              Complete
                            </Button>
                          )}
                          <Dropdown
                            trigger={
                              <Button variant="ghost" size="icon-sm">
                                <MoreHorizontal className="h-4 w-4" />
                              </Button>
                            }
                            align="right"
                            items={[
                              {
                                label: "View tasks",
                                icon: <GitBranch className="h-4 w-4" />,
                                onClick: () => window.location.href = `/workspaces/${workspaceId}/projects/${projectId}/sprints/${s.id}/tasks`,
                              },
                              {
                                label: "View metrics",
                                icon: <BarChart3 className="h-4 w-4" />,
                                onClick: () => window.location.href = `/workspaces/${workspaceId}/projects/${projectId}/sprints/${s.id}/metrics`,
                              },
                              { separator: true, label: "" },
                              ...(s.status === "ACTIVE" ? [{
                                label: "Cancel sprint",
                                icon: <XCircle className="h-4 w-4" />,
                                destructive: true,
                                onClick: () => cancelMutation.mutate(s.id),
                              }] : []),
                              {
                                label: "Delete sprint",
                                icon: <XCircle className="h-4 w-4" />,
                                destructive: true,
                                onClick: () => deleteMutation.mutate(s.id),
                              },
                            ]}
                          />
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          )}
        </div>
      </div>

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Sprint" size="md">
        <ModalBody className="space-y-4">
          <Input label="Sprint name" placeholder="Sprint 1" value={form.name} onChange={set("name")} autoFocus />
          <Textarea label="Goal" placeholder="What do you want to achieve?" value={form.goal} onChange={set("goal")} className="h-20" />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Start date" type="date" value={form.startDate} onChange={set("startDate")} />
            <Input label="End date" type="date" value={form.endDate} onChange={set("endDate")} />
          </div>
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowCreate(false)}>Cancel</Button>
          <Button onClick={() => createMutation.mutate()} loading={createMutation.isPending} disabled={!form.name.trim() || !form.startDate || !form.endDate}>
            Create Sprint
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
