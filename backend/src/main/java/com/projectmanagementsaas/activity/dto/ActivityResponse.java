package com.projectmanagementsaas.activity.dto;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(UUID id, UUID workspaceId, UUID projectId, UUID actorId, String action,
                               String entityType, UUID entityId, String message, Instant createdAt) {
}
