package com.projectmanagementsaas.task.dto;

import com.projectmanagementsaas.task.entity.TaskPriority;
import com.projectmanagementsaas.task.entity.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
        @NotNull UUID projectId,
        UUID parentTaskId,
        @NotBlank @Size(max = 220) String title,
        @Size(max = 4000) String description,
        TaskPriority priority,
        TaskType type,
        UUID assigneeId,
        LocalDate dueDate
) {
}
