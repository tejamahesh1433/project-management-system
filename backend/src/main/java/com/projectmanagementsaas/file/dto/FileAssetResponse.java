package com.projectmanagementsaas.file.dto;

import java.time.Instant;
import java.util.UUID;

public record FileAssetResponse(
        UUID id,
        UUID projectId,
        UUID folderId,
        String fileName,
        String storagePath,
        String contentType,
        long sizeBytes,
        UUID uploadedBy,
        Instant createdAt
) {
}
