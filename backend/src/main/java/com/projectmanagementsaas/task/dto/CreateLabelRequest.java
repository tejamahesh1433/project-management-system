package com.projectmanagementsaas.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateLabelRequest(
        @NotNull UUID projectId,
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(max = 32) String color
) {
}
