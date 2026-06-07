package com.projectmanagementsaas.analytics.mapper;

import com.projectmanagementsaas.analytics.dto.DashboardResponse;
import com.projectmanagementsaas.analytics.dto.DashboardWidgetResponse;
import com.projectmanagementsaas.analytics.entity.Dashboard;
import com.projectmanagementsaas.analytics.entity.DashboardWidget;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper {
    public DashboardResponse toResponse(Dashboard dashboard, List<DashboardWidget> widgets) {
        return new DashboardResponse(
                dashboard.getId(),
                dashboard.getWorkspaceId(),
                dashboard.getProjectId(),
                dashboard.getName(),
                dashboard.getCreatedBy().getId(),
                dashboard.getCreatedAt(),
                dashboard.getUpdatedAt(),
                widgets.stream().map(this::toWidgetResponse).toList());
    }

    public DashboardWidgetResponse toWidgetResponse(DashboardWidget widget) {
        return new DashboardWidgetResponse(
                widget.getId(),
                widget.getDashboard().getId(),
                widget.getType(),
                widget.getTitle(),
                widget.getPosition(),
                widget.getConfigJson(),
                widget.getCreatedAt(),
                widget.getUpdatedAt());
    }
}
