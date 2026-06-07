package com.projectmanagementsaas.document.dto;

import com.projectmanagementsaas.document.entity.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateDocumentRequest(UUID folderId, @NotBlank @Size(max = 220) String title, @NotBlank String content, DocumentStatus status) {
}
