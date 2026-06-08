"use client";
import { useState, useMemo } from "react";
import { useDebounce } from "@/lib/hooks";
import { useParams } from "next/navigation";
import Link from "next/link";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { projectApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { Dropdown } from "@/components/ui/dropdown";
import { useToast } from "@/components/ui/toast";
import { FolderKanban, Plus, Search, MoreHorizontal, Archive, Trash2, ExternalLink } from "lucide-react";
import type { Project } from "@/types";
import { formatDate } from "@/lib/utils";

const PROJECT_COLORS = ["#6366f1","#22c55e","#f59e0b","#ef4444","#8b5cf6","#06b6d4","#ec4899","#84cc16"];

export default function ProjectsPage() {
  const params = useParams<{ workspaceId: string }>();
  const workspaceId = params.workspaceId;
  const qc = useQueryClient();
  const { toast } = useToast();

  const [search, setSearch] = useState("");
  const debouncedSearch = useDebounce(search, 200);
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ name: "", description: "", color: PROJECT_COLORS[0] });

  const { data: projects = [], isLoading } = useQuery<Project[]>({
    queryKey: queryKeys.projects.all(workspaceId),
    queryFn: () => projectApi.list(workspaceId),
  });

  const createMutation = useMutation({
    mutationFn: () => {
      const slug = form.name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)+/g, "") || "project";
      return projectApi.create({ workspaceId, slug, ...form });
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.projects.all(workspaceId) });
      toast("success", "Project created");
      setShowCreate(false);
      setForm({ name: "", description: "", color: PROJECT_COLORS[0] });
    },
    onError: () => toast("error", "Failed to create project"),
  });

  const archiveMutation = useMutation({
    mutationFn: (id: string) => projectApi.archive(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.projects.all(workspaceId) });
      toast("success", "Project archived");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => projectApi.delete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.projects.all(workspaceId) });
      toast("success", "Project deleted");
    },
  });

  const filtered = useMemo(() =>
    projects.filter((p) =>
      p.name.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
      p.description?.toLowerCase().includes(debouncedSearch.toLowerCase())
    ),
    [projects, debouncedSearch]
  );

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Projects"
        workspaceId={workspaceId}
        actions={
          <Button size="sm" onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4" />
            New Project
          </Button>
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-5xl mx-auto">
          <div className="mb-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[var(--color-muted-foreground)]" />
              <input
                className="h-9 w-full max-w-xs rounded-md border border-[var(--color-input)] bg-transparent pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-ring)] placeholder:text-[var(--color-muted-foreground)]"
                placeholder="Search projects..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </div>

          {isLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {[1,2,3,4,5,6].map(i => <Skeleton key={i} className="h-36" />)}
            </div>
          ) : filtered.length === 0 ? (
            <EmptyState
              icon={FolderKanban}
              title="No projects found"
              description={search ? "Try a different search term" : "Create your first project to get started"}
              action={!search ? { label: "Create Project", onClick: () => setShowCreate(true) } : undefined}
            />
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {filtered.map((p) => (
                <Card key={p.id} className="hover:shadow-md transition-shadow group">
                  <CardContent className="pt-5">
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex items-center gap-2.5">
                        <div
                          className="h-9 w-9 rounded-lg flex items-center justify-center shrink-0"
                          style={{ backgroundColor: p.color ?? "#6366f1" }}
                        >
                          <FolderKanban className="h-5 w-5 text-white" />
                        </div>
                        <div>
                          <p className="text-sm font-semibold">{p.name}</p>
                          <Badge variant={p.status === "ACTIVE" ? "success" : p.status === "ARCHIVED" ? "secondary" : "destructive"} className="mt-0.5">
                            {p.status}
                          </Badge>
                        </div>
                      </div>
                      <Dropdown
                        trigger={
                          <Button variant="ghost" size="icon-sm" className="opacity-0 group-hover:opacity-100 transition-opacity">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        }
                        align="right"
                        items={[
                          {
                            label: "Open project",
                            icon: <ExternalLink className="h-4 w-4" />,
                            onClick: () => window.location.href = `/workspaces/${workspaceId}/projects/${p.id}/overview`,
                          },
                          { separator: true, label: "" },
                          {
                            label: "Archive",
                            icon: <Archive className="h-4 w-4" />,
                            onClick: () => archiveMutation.mutate(p.id),
                          },
                          {
                            label: "Delete",
                            icon: <Trash2 className="h-4 w-4" />,
                            destructive: true,
                            onClick: () => deleteMutation.mutate(p.id),
                          },
                        ]}
                      />
                    </div>
                    {p.description && (
                      <p className="text-xs text-[var(--color-muted-foreground)] line-clamp-2 mb-3">{p.description}</p>
                    )}
                    <div className="flex items-center justify-between">
                      <p className="text-[10px] text-[var(--color-muted-foreground)]">
                        Created {formatDate(p.createdAt)}
                      </p>
                      <Link href={`/workspaces/${workspaceId}/projects/${p.id}/overview`}>
                        <Button variant="ghost" size="sm" className="h-7 text-xs">Open →</Button>
                      </Link>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Create Project Modal */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Project" size="md">
        <ModalBody className="space-y-4">
          <Input
            label="Name"
            placeholder="My Project"
            value={form.name}
            onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
          />
          <Textarea
            label="Description"
            placeholder="What is this project about?"
            value={form.description}
            onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
            className="h-20"
          />
          <div>
            <label className="text-sm font-medium block mb-2">Color</label>
            <div className="flex items-center gap-2 flex-wrap">
              {PROJECT_COLORS.map((c) => (
                <button
                  key={c}
                  onClick={() => setForm((f) => ({ ...f, color: c }))}
                  className="h-7 w-7 rounded-full transition-transform hover:scale-110"
                  style={{ backgroundColor: c, outline: form.color === c ? `2px solid ${c}` : "none", outlineOffset: 2 }}
                />
              ))}
            </div>
          </div>
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowCreate(false)}>Cancel</Button>
          <Button onClick={() => createMutation.mutate()} loading={createMutation.isPending} disabled={!form.name.trim()}>
            Create Project
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
