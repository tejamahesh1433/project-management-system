package com.projectmanagementsaas.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateWorkspaceRequest(
        @NotNull UUID organizationId,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 120) String slug,
        @Size(max = 500) String description
) {
}
