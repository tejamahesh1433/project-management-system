package com.projectmanagementsaas.project.repository;

import com.projectmanagementsaas.project.entity.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    boolean existsByWorkspace_IdAndSlugIgnoreCaseAndDeletedAtIsNull(UUID workspaceId, String slug);

    List<Project> findByWorkspace_IdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID workspaceId);

    Optional<Project> findByIdAndDeletedAtIsNull(UUID id);
}
