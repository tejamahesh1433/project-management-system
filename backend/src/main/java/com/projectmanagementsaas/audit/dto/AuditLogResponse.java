package com.projectmanagementsaas.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(UUID id, UUID workspaceId, UUID projectId, UUID actorId, String action,
                               String entityType, UUID entityId, String beforeValue, String afterValue,
                               String ipAddress, String userAgent, Instant createdAt) {
}
