package com.projectmanagementsaas.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWorkspaceRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 120) String slug,
        @Size(max = 500) String description
) {
}
