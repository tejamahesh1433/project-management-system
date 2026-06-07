package com.projectmanagementsaas.ai.dto;

import com.projectmanagementsaas.ai.entity.AiConversationScope;
import com.projectmanagementsaas.ai.entity.AiModel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiConversationResponse(
        UUID id,
        UUID workspaceId,
        UUID projectId,
        AiConversationScope scope,
        String title,
        AiModel model,
        UUID createdById,
        Instant createdAt,
        Instant updatedAt,
        List<AiMessageResponse> messages
) {
}
