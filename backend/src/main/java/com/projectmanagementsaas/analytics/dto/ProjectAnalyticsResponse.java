package com.projectmanagementsaas.analytics.dto;

import java.util.Map;
import java.util.UUID;

public record ProjectAnalyticsResponse(
        UUID projectId,
        int totalTasks,
        Map<String, Long> taskDistribution,
        Map<String, Long> statusBreakdown,
        double sprintProgress
) {
}
