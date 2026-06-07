package com.projectmanagementsaas.document.repository;

import com.projectmanagementsaas.document.entity.DocumentVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
    List<DocumentVersion> findByDocument_IdOrderByVersionNumberDesc(UUID documentId);
    Optional<DocumentVersion> findByDocument_IdAndVersionNumber(UUID documentId, int versionNumber);
}
