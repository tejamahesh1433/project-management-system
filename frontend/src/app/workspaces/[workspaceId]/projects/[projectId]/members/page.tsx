"use client";
import { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { projectApi, workspaceApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Dropdown } from "@/components/ui/dropdown";
import { useToast } from "@/components/ui/toast";
import { formatDate } from "@/lib/utils";
import { Users, Plus, MoreHorizontal, UserMinus } from "lucide-react";

const ROLES = [
  { value: "LEAD", label: "Lead" },
  { value: "MEMBER", label: "Member" },
  { value: "VIEWER", label: "Viewer" },
];

export default function ProjectMembersPage() {
  const { workspaceId, projectId } = useParams<{ workspaceId: string; projectId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();

  const [showAdd, setShowAdd] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState("");
  const [selectedRole, setSelectedRole] = useState("MEMBER");

  const { data: members = [], isLoading } = useQuery({
    queryKey: queryKeys.projects.members(projectId),
    queryFn: () => projectApi.listMembers(projectId),
  });

  const { data: workspaceMembers = [] } = useQuery({
    queryKey: queryKeys.workspaces.members(workspaceId),
    queryFn: () => workspaceApi.listMembers(workspaceId),
  });

  const existingIds = new Set(members.map((m: { userId: string }) => m.userId));
  const available = workspaceMembers.filter((m: { userId: string }) => !existingIds.has(m.userId));

  const addMutation = useMutation({
    mutationFn: () => projectApi.addMember(projectId, { userId: selectedUserId, role: selectedRole }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.projects.members(projectId) });
      toast("success", "Member added");
      setShowAdd(false);
    },
    onError: () => toast("error", "Failed to add member"),
  });

  const removeMutation = useMutation({
    mutationFn: (memberId: string) => projectApi.removeMember(projectId, memberId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.projects.members(projectId) });
      toast("success", "Member removed");
    },
  });

  const updateRoleMutation = useMutation({
    mutationFn: ({ memberId, role }: { memberId: string; role: string }) =>
      projectApi.updateMemberRole(projectId, memberId, role),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.projects.members(projectId) });
      toast("success", "Role updated");
    },
  });

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Members"
        workspaceId={workspaceId}
        actions={
          <Button size="sm" onClick={() => setShowAdd(true)} disabled={available.length === 0}>
            <Plus className="h-4 w-4" />
            Add Member
          </Button>
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-2xl mx-auto">
          {isLoading ? (
            <div className="space-y-2">
              {[1,2,3].map(i => <div key={i} className="h-16 animate-pulse bg-[var(--color-muted)] rounded-lg" />)}
            </div>
          ) : (
            <div className="divide-y divide-[var(--color-border)] rounded-lg border border-[var(--color-border)] overflow-hidden">
              {members.map((m: { id: string; userId: string; role: string; joinedAt: string; displayName?: string; email?: string }) => (
                <div key={m.id} className="flex items-center gap-3 px-4 py-3 bg-[var(--color-card)] hover:bg-[var(--color-muted)]/30 transition-colors">
                  <Avatar name={m.displayName ?? "?"} size="sm" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium">{m.displayName}</p>
                    <p className="text-xs text-[var(--color-muted-foreground)]">{m.email}</p>
                  </div>
                  <Badge variant={m.role === "LEAD" ? "default" : "secondary"}>{m.role}</Badge>
                  <Dropdown
                    trigger={<Button variant="ghost" size="icon-sm"><MoreHorizontal className="h-4 w-4" /></Button>}
                    align="right"
                    items={[
                      ...ROLES.filter(r => r.value !== m.role).map(r => ({
                        label: `Set as ${r.label}`,
                        onClick: () => updateRoleMutation.mutate({ memberId: m.id, role: r.value }),
                      })),
                      { separator: true, label: "" },
                      { label: "Remove", icon: <UserMinus className="h-4 w-4" />, destructive: true, onClick: () => removeMutation.mutate(m.id) },
                    ]}
                  />
                </div>
              ))}
              {members.length === 0 && (
                <div className="py-12 text-center text-sm text-[var(--color-muted-foreground)] bg-[var(--color-card)]">No members yet</div>
              )}
            </div>
          )}
        </div>
      </div>

      <Modal open={showAdd} onClose={() => setShowAdd(false)} title="Add Member" size="sm">
        <ModalBody className="space-y-4">
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium">Select member</label>
            <select
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              className="h-9 w-full rounded-md border border-[var(--color-input)] bg-[var(--color-background)] px-3 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-ring)]"
            >
              <option value="">Select a workspace member...</option>
              {available.map((m: { userId: string; displayName?: string }) => (
                <option key={m.userId} value={m.userId}>
                  {m.displayName}
                </option>
              ))}
            </select>
          </div>
          <Select label="Role" value={selectedRole} options={ROLES} onChange={(e) => setSelectedRole(e.target.value)} />
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowAdd(false)}>Cancel</Button>
          <Button onClick={() => addMutation.mutate()} loading={addMutation.isPending} disabled={!selectedUserId}>Add</Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
