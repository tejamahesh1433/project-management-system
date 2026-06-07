package com.projectmanagementsaas.task.dto;

import com.projectmanagementsaas.task.entity.TaskPriority;
import com.projectmanagementsaas.task.entity.TaskStatus;
import com.projectmanagementsaas.task.entity.TaskType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        UUID parentTaskId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        TaskType type,
        UUID assigneeId,
        UUID createdBy,
        LocalDate dueDate,
        List<LabelResponse> labels,
        Instant createdAt,
        Instant updatedAt
) {
}
