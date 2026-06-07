package com.projectmanagementsaas.integration.dto;

import java.time.Instant;
import java.util.UUID;

public record WebhookSubscriptionResponse(
        UUID id,
        String provider,
        boolean enabled,
        Instant lastReceivedAt,
        Instant createdAt
) {
}
