package com.projectmanagementsaas.backup.repository;

import com.projectmanagementsaas.backup.entity.BackupMetadata;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupMetadataRepository extends JpaRepository<BackupMetadata, UUID> {
    List<BackupMetadata> findAllByOrderByCreatedAtDesc();

    Optional<BackupMetadata> findById(UUID id);
}
