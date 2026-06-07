package com.projectmanagementsaas.file.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateFileAssetRequest(
        @NotNull UUID projectId,
        UUID folderId,
        @NotBlank @Size(max = 255) String fileName,
        @Size(max = 120) String contentType,
        @Min(0) long sizeBytes
) {
}
