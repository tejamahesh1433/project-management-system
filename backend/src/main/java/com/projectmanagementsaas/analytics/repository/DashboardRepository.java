package com.projectmanagementsaas.analytics.repository;

import com.projectmanagementsaas.analytics.entity.Dashboard;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {
    List<Dashboard> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);

    Optional<Dashboard> findByIdAndWorkspaceId(UUID id, UUID workspaceId);
}
