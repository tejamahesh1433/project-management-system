package com.projectmanagementsaas.integration.dto;

import java.time.Instant;
import java.util.UUID;

public record IntegrationTestResponse(
        UUID integrationId,
        boolean success,
        String message,
        Instant checkedAt
) {
}
