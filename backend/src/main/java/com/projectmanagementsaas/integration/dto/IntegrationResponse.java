package com.projectmanagementsaas.integration.dto;

import com.projectmanagementsaas.integration.entity.IntegrationStatus;
import com.projectmanagementsaas.integration.entity.IntegrationType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IntegrationResponse(
        UUID id,
        UUID workspaceId,
        UUID projectId,
        IntegrationType type,
        String name,
        IntegrationStatus status,
        String repositoryUrl,
        String repositoryName,
        String metadataJson,
        UUID createdById,
        Instant createdAt,
        Instant updatedAt,
        List<IntegrationConnectionResponse> connections,
        List<WebhookSubscriptionResponse> webhooks
) {
}
