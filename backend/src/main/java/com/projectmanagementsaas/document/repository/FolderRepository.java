package com.projectmanagementsaas.document.repository;

import com.projectmanagementsaas.document.entity.Folder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    Optional<Folder> findByIdAndDeletedAtIsNull(UUID id);
    List<Folder> findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID projectId);
}
