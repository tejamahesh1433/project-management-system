package com.projectmanagementsaas.workspace.dto;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        UUID ownerId,
        Instant createdAt,
        Instant updatedAt
) {
}
