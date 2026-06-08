"use client";
import { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { reportApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { EmptyState } from "@/components/ui/empty-state";
import { Dropdown } from "@/components/ui/dropdown";
import { useToast } from "@/components/ui/toast";
import { formatDate } from "@/lib/utils";
import { FileText, Plus, MoreHorizontal, Download, Trash2 } from "lucide-react";
import type { Report } from "@/types";

const REPORT_TYPES = [
  { value: "TASK_SUMMARY", label: "Task Summary" },
  { value: "SPRINT_BURNDOWN", label: "Sprint Burndown" },
  { value: "TEAM_VELOCITY", label: "Team Velocity" },
  { value: "PROJECT_PROGRESS", label: "Project Progress" },
  { value: "CUSTOM", label: "Custom" },
];

export default function ReportsPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();

  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ name: "", type: "TASK_SUMMARY" });

  const { data: reports = [], isLoading } = useQuery<Report[]>({
    queryKey: queryKeys.reports.all(workspaceId),
    queryFn: () => reportApi.list(workspaceId),
  });

  const createMutation = useMutation({
    mutationFn: () => reportApi.create({ workspaceId, ...form }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.reports.all(workspaceId) });
      toast("success", "Report created");
      setShowCreate(false);
      setForm({ name: "", type: "TASK_SUMMARY" });
    },
    onError: () => toast("error", "Failed to create report"),
  });

  const exportMutation = useMutation({
    mutationFn: (id: string) => reportApi.export(id, "CSV"),
    onSuccess: (data, id) => {
      const url = URL.createObjectURL(new Blob([data]));
      const a = document.createElement("a");
      a.href = url;
      a.download = `report-${id}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      toast("success", "Report exported");
    },
    onError: () => toast("error", "Export failed"),
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Reports"
        workspaceId={workspaceId}
        actions={
          <Button size="sm" onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4" />
            New Report
          </Button>
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto">
          {isLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {[1,2,3].map(i => <div key={i} className="h-28 animate-pulse bg-[var(--color-muted)] rounded-lg" />)}
            </div>
          ) : reports.length === 0 ? (
            <EmptyState
              icon={FileText}
              title="No reports"
              description="Create reports to track metrics and share insights"
              action={{ label: "New Report", onClick: () => setShowCreate(true) }}
            />
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {reports.map((r) => (
                <Card key={r.id} className="hover:shadow-md transition-shadow group">
                  <CardContent className="pt-5">
                    <div className="flex items-start justify-between">
                      <div className="flex items-center gap-3">
                        <div className="h-9 w-9 rounded-lg bg-[var(--color-primary)]/10 flex items-center justify-center">
                          <FileText className="h-5 w-5 text-[var(--color-primary)]" />
                        </div>
                        <div>
                          <p className="font-semibold">{r.name}</p>
                          <p className="text-xs text-[var(--color-muted-foreground)]">{r.type.replace("_", " ")}</p>
                        </div>
                      </div>
                      <Dropdown
                        trigger={<Button variant="ghost" size="icon-sm" className="opacity-0 group-hover:opacity-100"><MoreHorizontal className="h-4 w-4" /></Button>}
                        align="right"
                        items={[
                          { label: "Export CSV", icon: <Download className="h-4 w-4" />, onClick: () => exportMutation.mutate(r.id) },
                        ]}
                      />
                    </div>
                    <p className="text-xs text-[var(--color-muted-foreground)] mt-3">Created {formatDate(r.createdAt)}</p>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Report" size="sm">
        <ModalBody className="space-y-4">
          <Input label="Report name" placeholder="Q1 Task Summary" value={form.name} onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))} />
          <Select label="Report type" value={form.type} options={REPORT_TYPES} onChange={(e) => setForm(f => ({ ...f, type: e.target.value }))} />
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowCreate(false)}>Cancel</Button>
          <Button onClick={() => createMutation.mutate()} loading={createMutation.isPending} disabled={!form.name.trim()}>Create</Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
