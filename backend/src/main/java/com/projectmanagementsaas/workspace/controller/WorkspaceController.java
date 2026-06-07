package com.projectmanagementsaas.workspace.controller;

import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.workspace.dto.AcceptInvitationRequest;
import com.projectmanagementsaas.workspace.dto.CreateWorkspaceRequest;
import com.projectmanagementsaas.workspace.dto.InviteWorkspaceMemberRequest;
import com.projectmanagementsaas.workspace.dto.UpdateWorkspaceMemberRoleRequest;
import com.projectmanagementsaas.workspace.dto.UpdateWorkspaceRequest;
import com.projectmanagementsaas.workspace.dto.WorkspaceInvitationResponse;
import com.projectmanagementsaas.workspace.dto.WorkspaceMemberResponse;
import com.projectmanagementsaas.workspace.dto.WorkspaceResponse;
import com.projectmanagementsaas.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    ResponseEntity<WorkspaceResponse> createWorkspace(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateWorkspaceRequest request
    ) {
        return ResponseEntity.ok(workspaceService.createWorkspace(currentUser.id(), request));
    }

    @GetMapping
    ResponseEntity<List<WorkspaceResponse>> listWorkspaces(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(workspaceService.listWorkspaces(currentUser.id()));
    }

    @GetMapping("/{workspaceId}")
    ResponseEntity<WorkspaceResponse> getWorkspace(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(workspaceService.getWorkspace(currentUser.id(), workspaceId));
    }

    @PutMapping("/{workspaceId}")
    ResponseEntity<WorkspaceResponse> updateWorkspace(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request
    ) {
        return ResponseEntity.ok(workspaceService.updateWorkspace(currentUser.id(), workspaceId, request));
    }

    @DeleteMapping("/{workspaceId}")
    ResponseEntity<MessageResponse> deleteWorkspace(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId
    ) {
        workspaceService.deleteWorkspace(currentUser.id(), workspaceId);
        return ResponseEntity.ok(new MessageResponse("Workspace deleted"));
    }

    @PostMapping("/{workspaceId}/invitations")
    ResponseEntity<WorkspaceInvitationResponse> inviteMember(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteWorkspaceMemberRequest request
    ) {
        return ResponseEntity.ok(workspaceService.inviteMember(currentUser.id(), workspaceId, request));
    }

    @GetMapping("/{workspaceId}/invitations")
    ResponseEntity<List<WorkspaceInvitationResponse>> listPendingInvitations(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(workspaceService.listPendingInvitations(currentUser.id(), workspaceId));
    }

    @DeleteMapping("/{workspaceId}/invitations/{invitationId}")
    ResponseEntity<MessageResponse> revokeInvitation(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId,
            @PathVariable UUID invitationId
    ) {
        workspaceService.revokeInvitation(currentUser.id(), workspaceId, invitationId);
        return ResponseEntity.ok(new MessageResponse("Invitation revoked"));
    }

    @PostMapping("/invitations/accept")
    ResponseEntity<WorkspaceMemberResponse> acceptInvitation(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody AcceptInvitationRequest request
    ) {
        return ResponseEntity.ok(workspaceService.acceptInvitation(currentUser.id(), request));
    }

    @GetMapping("/{workspaceId}/members")
    ResponseEntity<List<WorkspaceMemberResponse>> listMembers(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(workspaceService.listMembers(currentUser.id(), workspaceId));
    }

    @PatchMapping("/{workspaceId}/members/{memberId}/role")
    ResponseEntity<WorkspaceMemberResponse> updateMemberRole(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateWorkspaceMemberRoleRequest request
    ) {
        return ResponseEntity.ok(workspaceService.updateMemberRole(currentUser.id(), workspaceId, memberId, request));
    }

    @DeleteMapping("/{workspaceId}/members/{memberId}")
    ResponseEntity<MessageResponse> removeMember(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberId
    ) {
        workspaceService.removeMember(currentUser.id(), workspaceId, memberId);
        return ResponseEntity.ok(new MessageResponse("Workspace member removed"));
    }
}
