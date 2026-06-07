package com.projectmanagementsaas.integration.dto;

import com.projectmanagementsaas.integration.entity.IntegrationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateIntegrationRequest(
        @NotNull UUID workspaceId,
        UUID projectId,
        @NotNull IntegrationType type,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 500) String endpointUrl,
        @Size(max = 500) String repositoryUrl,
        @Size(max = 220) String repositoryName,
        String metadataJson
) {
}
