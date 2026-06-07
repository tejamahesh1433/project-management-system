package com.projectmanagementsaas.ai.dto;

public record AiChatResponse(
        AiConversationResponse conversation,
        String answer
) {
}
