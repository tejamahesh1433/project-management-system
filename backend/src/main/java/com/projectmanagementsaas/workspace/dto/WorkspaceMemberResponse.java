package com.projectmanagementsaas.workspace.dto;

import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import java.time.Instant;
import java.util.UUID;

public record WorkspaceMemberResponse(
        UUID id,
        UUID workspaceId,
        UUID userId,
        String email,
        String displayName,
        WorkspaceRole role,
        Instant createdAt
) {
}
