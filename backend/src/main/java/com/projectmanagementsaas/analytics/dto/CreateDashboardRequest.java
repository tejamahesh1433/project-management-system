package com.projectmanagementsaas.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateDashboardRequest(
        @NotNull UUID workspaceId,
        UUID projectId,
        @NotBlank @Size(max = 160) String name
) {
}
