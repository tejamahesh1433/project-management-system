package com.projectmanagementsaas.ai.dto;

import com.projectmanagementsaas.ai.entity.AiMessageRole;
import java.time.Instant;
import java.util.UUID;

public record AiMessageResponse(
        UUID id,
        AiMessageRole role,
        String content,
        Instant createdAt
) {
}
