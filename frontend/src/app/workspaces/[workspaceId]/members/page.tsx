"use client";
import { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { workspaceApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Dropdown } from "@/components/ui/dropdown";
import { useToast } from "@/components/ui/toast";
import { formatDate } from "@/lib/utils";
import { Users, Plus, MoreHorizontal, UserMinus, Mail } from "lucide-react";
import type { WorkspaceMember, WorkspaceInvitation } from "@/types";

const ROLES = [
  { value: "ADMIN", label: "Admin" },
  { value: "MEMBER", label: "Member" },
  { value: "VIEWER", label: "Viewer" },
];

export default function WorkspaceMembersPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const qc = useQueryClient();
  const { toast } = useToast();

  const [showInvite, setShowInvite] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteRole, setInviteRole] = useState("MEMBER");

  const { data: members = [], isLoading } = useQuery<WorkspaceMember[]>({
    queryKey: queryKeys.workspaces.members(workspaceId),
    queryFn: () => workspaceApi.listMembers(workspaceId),
  });

  const { data: invitations = [] } = useQuery<WorkspaceInvitation[]>({
    queryKey: queryKeys.workspaces.invitations(workspaceId),
    queryFn: () => workspaceApi.listInvitations(workspaceId),
  });

  const inviteMutation = useMutation({
    mutationFn: () => workspaceApi.inviteMember(workspaceId, { email: inviteEmail, role: inviteRole }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.workspaces.invitations(workspaceId) });
      toast("success", "Invitation sent", inviteEmail);
      setShowInvite(false);
      setInviteEmail("");
    },
    onError: () => toast("error", "Failed to send invitation"),
  });

  const revokeInviteMutation = useMutation({
    mutationFn: (invitationId: string) => workspaceApi.revokeInvitation(workspaceId, invitationId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.workspaces.invitations(workspaceId) });
      toast("success", "Invitation revoked");
    },
  });

  const removeMutation = useMutation({
    mutationFn: (memberId: string) => workspaceApi.removeMember(workspaceId, memberId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.workspaces.members(workspaceId) });
      toast("success", "Member removed");
    },
  });

  const updateRoleMutation = useMutation({
    mutationFn: ({ memberId, role }: { memberId: string; role: string }) =>
      workspaceApi.updateMemberRole(workspaceId, memberId, role),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.workspaces.members(workspaceId) });
      toast("success", "Role updated");
    },
  });

  const pendingInvites = invitations.filter((i) => i.status === "PENDING");

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        title="Members"
        workspaceId={workspaceId}
        actions={
          <Button size="sm" onClick={() => setShowInvite(true)}>
            <Plus className="h-4 w-4" />
            Invite Member
          </Button>
        }
      />
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-3xl mx-auto space-y-6">
          {/* Members */}
          <div>
            <h2 className="text-sm font-semibold mb-3">Members ({members.length})</h2>
            <div className="divide-y divide-[var(--color-border)] rounded-lg border border-[var(--color-border)] overflow-hidden">
              {isLoading ? (
                <div className="p-4 text-sm text-[var(--color-muted-foreground)]">Loading...</div>
              ) : members.map((m) => (
                <div key={m.id} className="flex items-center gap-3 px-4 py-3 bg-[var(--color-card)]">
                  <Avatar name={m.displayName ?? m.email} size="sm" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium">{m.displayName}</p>
                    <p className="text-xs text-[var(--color-muted-foreground)]">{m.email}</p>
                  </div>
                  <Badge variant={m.role === "OWNER" ? "default" : m.role === "ADMIN" ? "info" : "secondary"}>{m.role}</Badge>
                  {m.role !== "OWNER" && (
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
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Pending invitations */}
          {pendingInvites.length > 0 && (
            <div>
              <h2 className="text-sm font-semibold mb-3">Pending Invitations ({pendingInvites.length})</h2>
              <div className="divide-y divide-[var(--color-border)] rounded-lg border border-[var(--color-border)] overflow-hidden">
                {pendingInvites.map((inv) => (
                  <div key={inv.id} className="flex items-center gap-3 px-4 py-3 bg-[var(--color-card)]">
                    <div className="h-8 w-8 rounded-full bg-[var(--color-muted)] flex items-center justify-center shrink-0">
                      <Mail className="h-4 w-4 text-[var(--color-muted-foreground)]" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium">{inv.email}</p>
                      <p className="text-xs text-[var(--color-muted-foreground)]">Invited · {inv.role}</p>
                    </div>
                    <Badge variant="warning">Pending</Badge>
                    <Button variant="ghost" size="sm" onClick={() => revokeInviteMutation.mutate(inv.id)}>
                      Revoke
                    </Button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      <Modal open={showInvite} onClose={() => setShowInvite(false)} title="Invite Member" size="sm">
        <ModalBody className="space-y-4">
          <Input
            label="Email address"
            type="email"
            placeholder="colleague@example.com"
            value={inviteEmail}
            onChange={(e) => setInviteEmail(e.target.value)}
            autoFocus
          />
          <Select label="Role" value={inviteRole} options={ROLES} onChange={(e) => setInviteRole(e.target.value)} />
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowInvite(false)}>Cancel</Button>
          <Button onClick={() => inviteMutation.mutate()} loading={inviteMutation.isPending} disabled={!inviteEmail.trim()}>
            Send Invitation
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
}
