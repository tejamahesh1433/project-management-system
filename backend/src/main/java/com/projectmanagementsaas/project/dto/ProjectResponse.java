package com.projectmanagementsaas.project.dto;

import com.projectmanagementsaas.project.entity.ProjectRole;
import com.projectmanagementsaas.project.entity.ProjectStatus;
import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID workspaceId,
        String name,
        String slug,
        String description,
        ProjectStatus status,
        String color,
        String icon,
        UUID createdBy,
        ProjectRole currentUserRole,
        Instant createdAt,
        Instant updatedAt
) {
}
