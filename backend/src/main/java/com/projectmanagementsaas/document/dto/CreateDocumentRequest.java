package com.projectmanagementsaas.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateDocumentRequest(
        @NotNull UUID projectId,
        UUID folderId,
        @NotBlank @Size(max = 220) String title,
        @NotBlank String content
) {
}
