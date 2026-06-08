"use client";
import { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { integrationApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { EmptyState } from "@/components/ui/empty-state";
import { Dropdown } from "@/components/ui/dropdown";
import { useToast } from "@/components/ui/toast";
import { formatRelativeTime } from "@/lib/utils";
import { Plug, Plus, MoreHorizontal, Trash2, TestTube, CheckCircle, XCircle, AlertCircle } from "lucide-react";
import type { Integration } from "@/types";

const INTEGRATION_TYPES = [
  { value: "GITHUB", label: "GitHub" },
  { value: "GITLAB", label: "GitLab" },
  { value: "SLACK", label: "Slack" },
  { value: "JIRA", label: "Jira" },
  { value: "WEBHOOK", label: "Webhook" },
];

const STATUS_ICON: Record<string, React.FC<{ className?: string }>> = {
  ACTIVE: CheckCircle,
  INACTIVE: XCircle,
  ERROR: AlertCircle,
};

const STATUS_COLOR: Record<string, string> = {
  ACTIVE: "text-green-500",
  INACTIVE: "text-gray-400",
  ERROR: "text-red-500",
};

export default function IntegrationsPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();

  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ type: "GITHUB", name: "", webhookUrl: "", token: "" });

  const { data: integrations = [], isLoading } = useQuery<Integration[]>({
    queryKey: queryKeys.integrations.all(workspaceId),
    queryFn: () => integrationApi.list(workspaceId),
  });

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  const createMutation = useMutation({
    mutationFn: () => integrationApi.create({
      workspaceId,
      type: form.type,
      name: form.name,
      config: form.webhookUrl ? { webhookUrl: form.webhookUrl, token: form.token } : { token: form.token },
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.integrations.all(workspaceId) });
      toast("success", "Integration created");
      setShowCreate(false);
      setForm({ type: "GITHUB", name: "", webhookUrl: "", token: "" });
    },
    onError: () => toast("error", "Failed to create integration"),
  });

  const testMutation = useMutation({
    mutationFn: (id: string) => integrationApi.test(id),
    onSuccess: (data) => {
      toast(data.success ? "success" : "error", data.message);
    },
    onError: () => toast("error", "Test failed"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => integrationApi.delete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.integrations.all(workspaceId) });
      toast("success", "Integration removed");
    },
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Integrations"
        workspaceId={workspaceId}
        actions={
          <Button size="sm" onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4" />
            Add Integration
          </Button>
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto">
          {isLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {[1,2,3].map(i => <div key={i} className="h-32 animate-pulse bg-[var(--color-muted)] rounded-lg" />)}
            </div>
          ) : integrations.length === 0 ? (
            <EmptyState
              icon={Plug}
              title="No integrations"
              description="Connect your tools to automate workflows"
              action={{ label: "Add Integration", onClick: () => setShowCreate(true) }}
            />
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {integrations.map((integ) => {
                const StatusIcon = STATUS_ICON[integ.status] ?? AlertCircle;
                const statusColor = STATUS_COLOR[integ.status] ?? "text-gray-400";
                return (
                  <Card key={integ.id} className="hover:shadow-md transition-shadow">
                    <CardContent className="pt-5">
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-3">
                          <div className="h-10 w-10 rounded-lg bg-[var(--color-muted)] flex items-center justify-center">
                            <Plug className="h-5 w-5 text-[var(--color-primary)]" />
                          </div>
                          <div>
                            <p className="font-semibold">{integ.name}</p>
                            <div className="flex items-center gap-1.5 mt-0.5">
                              <StatusIcon className={`h-3.5 w-3.5 ${statusColor}`} />
                              <span className={`text-xs font-medium ${statusColor}`}>{integ.status}</span>
                              <span className="text-xs text-[var(--color-muted-foreground)]">· {integ.type}</span>
                            </div>
                          </div>
                        </div>
                        <Dropdown
                          trigger={
                            <Button variant="ghost" size="icon-sm">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          }
                          align="right"
                          items={[
                            {
                              label: "Test connection",
                              icon: <TestTube className="h-4 w-4" />,
                              onClick: () => testMutation.mutate(integ.id),
                            },
                            { separator: true, label: "" },
                            {
                              label: "Remove",
                              icon: <Trash2 className="h-4 w-4" />,
                              destructive: true,
                              onClick: () => deleteMutation.mutate(integ.id),
                            },
                          ]}
                        />
                      </div>
                      <p className="text-xs text-[var(--color-muted-foreground)] mt-3">
                        Added {formatRelativeTime(integ.createdAt)}
                      </p>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          )}
        </div>
      </div>

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Add Integration" size="md">
        <ModalBody className="space-y-4">
          <Select
            label="Integration type"
            value={form.type}
            options={INTEGRATION_TYPES}
            onChange={set("type")}
          />
          <Input label="Name" placeholder="My GitHub Integration" value={form.name} onChange={set("name")} />
          {form.type === "WEBHOOK" && (
            <Input label="Webhook URL" placeholder="https://..." value={form.webhookUrl} onChange={set("webhookUrl")} />
          )}
          <Input label="API Token / Secret" type="password" placeholder="••••••••" value={form.token} onChange={set("token")} />
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowCreate(false)}>Cancel</Button>
          <Button onClick={() => createMutation.mutate()} loading={createMutation.isPending} disabled={!form.name.trim()}>
            Add Integration
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
