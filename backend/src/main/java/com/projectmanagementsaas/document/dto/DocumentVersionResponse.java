package com.projectmanagementsaas.document.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionResponse(
        UUID id,
        UUID documentId,
        int versionNumber,
        String title,
        String content,
        UUID createdBy,
        Instant createdAt
) {
}
