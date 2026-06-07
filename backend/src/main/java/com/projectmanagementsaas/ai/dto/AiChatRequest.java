package com.projectmanagementsaas.ai.dto;

import com.projectmanagementsaas.ai.entity.AiModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AiChatRequest(
        UUID conversationId,
        @NotNull UUID workspaceId,
        UUID projectId,
        AiModel model,
        @NotBlank String message
) {
}
