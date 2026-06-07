package com.projectmanagementsaas.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AiSearchRequest(
        @NotNull UUID workspaceId,
        UUID projectId,
        @NotBlank String query
) {
}
