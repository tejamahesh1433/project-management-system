package com.projectmanagementsaas.task.dto;

import com.projectmanagementsaas.task.entity.TaskPriority;
import com.projectmanagementsaas.task.entity.TaskStatus;
import com.projectmanagementsaas.task.entity.TaskType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(
        UUID parentTaskId,
        @NotBlank @Size(max = 220) String title,
        @Size(max = 4000) String description,
        TaskStatus status,
        TaskPriority priority,
        TaskType type,
        UUID assigneeId,
        LocalDate dueDate,
        @Min(0) Integer storyPoints
) {
}
