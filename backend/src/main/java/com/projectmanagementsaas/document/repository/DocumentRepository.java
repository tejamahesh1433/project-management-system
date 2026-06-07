package com.projectmanagementsaas.document.repository;

import com.projectmanagementsaas.document.entity.Document;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndDeletedAtIsNull(UUID id);
    List<Document> findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID projectId);
}
