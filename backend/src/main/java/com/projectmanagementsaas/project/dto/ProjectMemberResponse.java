package com.projectmanagementsaas.project.dto;

import com.projectmanagementsaas.project.entity.ProjectRole;
import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UUID projectId,
        UUID userId,
        String email,
        String displayName,
        ProjectRole role,
        Instant joinedAt
) {
}
