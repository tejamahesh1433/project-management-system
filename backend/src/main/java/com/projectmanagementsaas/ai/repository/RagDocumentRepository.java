package com.projectmanagementsaas.ai.repository;

import com.projectmanagementsaas.ai.entity.RagDocument;
import com.projectmanagementsaas.ai.entity.RagSourceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RagDocumentRepository extends JpaRepository<RagDocument, UUID> {
    List<RagDocument> findByWorkspaceId(UUID workspaceId);

    List<RagDocument> findByWorkspaceIdAndProjectId(UUID workspaceId, UUID projectId);

    Optional<RagDocument> findBySourceTypeAndSourceId(RagSourceType sourceType, UUID sourceId);
}
