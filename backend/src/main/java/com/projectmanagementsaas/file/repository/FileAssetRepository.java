package com.projectmanagementsaas.file.repository;

import com.projectmanagementsaas.file.entity.FileAsset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAssetRepository extends JpaRepository<FileAsset, UUID> {
    Optional<FileAsset> findByIdAndDeletedAtIsNull(UUID id);
    List<FileAsset> findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID projectId);
}
