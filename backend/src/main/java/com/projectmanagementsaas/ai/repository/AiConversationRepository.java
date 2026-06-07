package com.projectmanagementsaas.ai.repository;

import com.projectmanagementsaas.ai.entity.AiConversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiConversationRepository extends JpaRepository<AiConversation, UUID> {
    List<AiConversation> findByWorkspaceIdAndCreatedBy_IdOrderByUpdatedAtDesc(UUID workspaceId, UUID userId);

    Optional<AiConversation> findByIdAndCreatedBy_Id(UUID id, UUID userId);
}
