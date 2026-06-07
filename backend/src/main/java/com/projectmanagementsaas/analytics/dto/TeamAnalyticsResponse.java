package com.projectmanagementsaas.analytics.dto;

import java.util.UUID;

public record TeamAnalyticsResponse(
        UUID projectId,
        int assignedTasks,
        int completedTasks,
        int openTasks,
        double averageCompletionTimeHours,
        int overdueTasks
) {
}
