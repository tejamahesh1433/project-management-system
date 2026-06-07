package com.projectmanagementsaas.document.dto;

import com.projectmanagementsaas.document.entity.DocumentStatus;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID projectId,
        UUID folderId,
        String title,
        String content,
        DocumentStatus status,
        int currentVersion,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
