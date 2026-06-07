package com.projectmanagementsaas.sprint.dto;

import java.util.UUID;

public record SprintMetricsResponse(
        UUID sprintId,
        int totalTasks,
        int completedTasks,
        int remainingTasks,
        double completionPercentage,
        int storyPointsCompleted,
        int storyPointsRemaining
) {
}
