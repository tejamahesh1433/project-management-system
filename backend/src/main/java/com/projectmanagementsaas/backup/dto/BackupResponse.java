package com.projectmanagementsaas.backup.dto;

import com.projectmanagementsaas.backup.entity.BackupStatus;
import java.time.Instant;
import java.util.UUID;

public record BackupResponse(
        UUID id,
        String fileName,
        long sizeBytes,
        BackupStatus status,
        String message,
        UUID createdById,
        Instant createdAt,
        Instant restoredAt
) {
}
