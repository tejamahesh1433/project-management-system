package com.projectmanagementsaas.integration.mapper;

import com.projectmanagementsaas.integration.dto.IntegrationConnectionResponse;
import com.projectmanagementsaas.integration.dto.IntegrationResponse;
import com.projectmanagementsaas.integration.dto.WebhookSubscriptionResponse;
import com.projectmanagementsaas.integration.entity.Integration;
import com.projectmanagementsaas.integration.entity.IntegrationConnection;
import com.projectmanagementsaas.webhook.entity.WebhookSubscription;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class IntegrationMapper {
    public IntegrationResponse toResponse(Integration integration, List<IntegrationConnection> connections,
            List<WebhookSubscription> webhooks) {
        return new IntegrationResponse(
                integration.getId(),
                integration.getWorkspaceId(),
                integration.getProjectId(),
                integration.getType(),
                integration.getName(),
                integration.getStatus(),
                integration.getRepositoryUrl(),
                integration.getRepositoryName(),
                integration.getMetadataJson(),
                integration.getCreatedBy().getId(),
                integration.getCreatedAt(),
                integration.getUpdatedAt(),
                connections.stream().map(this::toConnectionResponse).toList(),
                webhooks.stream().map(this::toWebhookResponse).toList());
    }

    public IntegrationConnectionResponse toConnectionResponse(IntegrationConnection connection) {
        return new IntegrationConnectionResponse(connection.getId(), connection.getEndpointUrl(), connection.getExternalId(),
                connection.getStatus(), connection.getLastMessage(), connection.getLastCheckedAt());
    }

    public WebhookSubscriptionResponse toWebhookResponse(WebhookSubscription webhook) {
        return new WebhookSubscriptionResponse(webhook.getId(), webhook.getProvider(), webhook.isEnabled(),
                webhook.getLastReceivedAt(), webhook.getCreatedAt());
    }
}
