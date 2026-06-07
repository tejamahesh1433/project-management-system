package com.projectmanagementsaas.ai.mapper;

import com.projectmanagementsaas.ai.dto.AiConversationResponse;
import com.projectmanagementsaas.ai.dto.AiMessageResponse;
import com.projectmanagementsaas.ai.entity.AiConversation;
import com.projectmanagementsaas.ai.entity.AiMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiMapper {
    public AiConversationResponse toConversationResponse(AiConversation conversation, List<AiMessage> messages) {
        return new AiConversationResponse(
                conversation.getId(),
                conversation.getWorkspaceId(),
                conversation.getProjectId(),
                conversation.getScope(),
                conversation.getTitle(),
                conversation.getModel(),
                conversation.getCreatedBy().getId(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messages.stream().map(this::toMessageResponse).toList());
    }

    private AiMessageResponse toMessageResponse(AiMessage message) {
        return new AiMessageResponse(message.getId(), message.getRole(), message.getContent(), message.getCreatedAt());
    }
}
