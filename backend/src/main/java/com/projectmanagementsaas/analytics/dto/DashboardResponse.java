package com.projectmanagementsaas.analytics.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        UUID id,
        UUID workspaceId,
        UUID projectId,
        String name,
        UUID createdById,
        Instant createdAt,
        Instant updatedAt,
        List<DashboardWidgetResponse> widgets
) {
}
