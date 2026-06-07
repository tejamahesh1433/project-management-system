package com.projectmanagementsaas.ai.dto;

import java.util.UUID;

public record AiSummaryResponse(
        String scope,
        UUID id,
        String summary
) {
}
