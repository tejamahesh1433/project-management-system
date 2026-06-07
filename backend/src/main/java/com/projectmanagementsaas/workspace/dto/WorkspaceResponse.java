package com.projectmanagementsaas.workspace.dto;

import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import java.time.Instant;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        UUID organizationId,
        String name,
        String slug,
        String description,
        WorkspaceRole currentUserRole,
        Instant createdAt,
        Instant updatedAt
) {
}
