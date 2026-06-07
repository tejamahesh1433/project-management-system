package com.projectmanagementsaas.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateFolderRequest(@NotNull UUID projectId, UUID parentFolderId, @NotBlank @Size(max = 160) String name) {
}
