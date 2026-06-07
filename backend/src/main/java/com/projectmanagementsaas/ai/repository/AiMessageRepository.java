package com.projectmanagementsaas.ai.repository;

import com.projectmanagementsaas.ai.entity.AiMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {
    List<AiMessage> findByConversation_IdOrderByCreatedAtAsc(UUID conversationId);
}
