package com.projectmanagementsaas.workspace.repository;

import com.projectmanagementsaas.workspace.entity.Workspace;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    boolean existsByOrganization_IdAndSlugIgnoreCase(UUID organizationId, String slug);

    List<Workspace> findByIdIn(Collection<UUID> ids);
}
