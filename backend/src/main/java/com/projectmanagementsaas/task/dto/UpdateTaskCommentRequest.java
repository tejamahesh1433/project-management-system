package com.projectmanagementsaas.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTaskCommentRequest(@NotBlank @Size(max = 4000) String body) {
}
