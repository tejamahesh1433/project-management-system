package com.projectmanagementsaas.document.dto;

import java.time.Instant;
import java.util.UUID;

public record FolderResponse(UUID id, UUID projectId, UUID parentFolderId, String name, Instant createdAt, Instant updatedAt) {
}
