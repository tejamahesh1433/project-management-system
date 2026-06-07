package com.projectmanagementsaas.sprint.dto;

import com.projectmanagementsaas.task.entity.TaskStatus;
import java.time.Instant;
import java.util.UUID;

public record SprintTaskResponse(
        UUID id,
        UUID sprintId,
        UUID taskId,
        String title,
        TaskStatus status,
        int storyPoints,
        Instant addedAt
) {
}
