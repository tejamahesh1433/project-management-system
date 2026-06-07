package com.projectmanagementsaas.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateFolderRequest(UUID parentFolderId, @NotBlank @Size(max = 160) String name) {
}
