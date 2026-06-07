package com.projectmanagementsaas.integration.dto;

import com.projectmanagementsaas.integration.entity.ConnectionStatus;
import java.time.Instant;
import java.util.UUID;

public record IntegrationConnectionResponse(
        UUID id,
        String endpointUrl,
        String externalId,
        ConnectionStatus status,
        String lastMessage,
        Instant lastCheckedAt
) {
}
