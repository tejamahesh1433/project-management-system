package com.projectmanagementsaas.integration.repository;

import com.projectmanagementsaas.integration.entity.Integration;
import com.projectmanagementsaas.integration.entity.IntegrationType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationRepository extends JpaRepository<Integration, UUID> {
    List<Integration> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);

    Optional<Integration> findByIdAndWorkspaceId(UUID id, UUID workspaceId);

    List<Integration> findByTypeOrderByCreatedAtDesc(IntegrationType type);
}
