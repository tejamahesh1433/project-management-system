package com.projectmanagementsaas.analytics.repository;

import com.projectmanagementsaas.analytics.entity.DashboardWidget;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardWidgetRepository extends JpaRepository<DashboardWidget, UUID> {
    List<DashboardWidget> findByDashboard_IdOrderByPositionAscCreatedAtAsc(UUID dashboardId);

    Optional<DashboardWidget> findByIdAndDashboard_Id(UUID id, UUID dashboardId);
}
