package com.projectmanagementsaas.workspace.dto;

import com.projectmanagementsaas.workspace.entity.InvitationStatus;
import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import java.time.Instant;
import java.util.UUID;

public record WorkspaceInvitationResponse(
        UUID id,
        UUID workspaceId,
        String email,
        WorkspaceRole role,
        InvitationStatus status,
        Instant expiresAt,
        String token
) {
}
