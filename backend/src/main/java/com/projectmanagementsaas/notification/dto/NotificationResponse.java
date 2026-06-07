package com.projectmanagementsaas.notification.dto;

import com.projectmanagementsaas.notification.entity.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, NotificationType type, String title, String message,
                                   String entityType, UUID entityId, UUID workspaceId, UUID projectId,
                                   boolean read, Instant createdAt) {
}
