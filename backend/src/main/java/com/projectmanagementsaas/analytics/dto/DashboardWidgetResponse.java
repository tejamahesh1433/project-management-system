package com.projectmanagementsaas.analytics.dto;

import com.projectmanagementsaas.analytics.entity.WidgetType;
import java.time.Instant;
import java.util.UUID;

public record DashboardWidgetResponse(
        UUID id,
        UUID dashboardId,
        WidgetType type,
        String title,
        int position,
        String configJson,
        Instant createdAt,
        Instant updatedAt
) {
}
