package com.projectmanagementsaas.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateProjectRequest(
        @NotNull UUID workspaceId,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 120) String slug,
        @Size(max = 1000) String description,
        @Size(max = 32) String color,
        @Size(max = 80) String icon
) {
}
