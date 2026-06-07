package com.projectmanagementsaas.analytics.dto;

import java.util.UUID;

public record SprintAnalyticsResponse(
        UUID sprintId,
        int velocity,
        double completionPercentage,
        int storyPointsCompleted,
        int storyPointsRemaining
) {
}
