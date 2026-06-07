package com.projectmanagementsaas.task.dto;

import com.projectmanagementsaas.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeTaskStatusRequest(@NotNull TaskStatus status) {
}
