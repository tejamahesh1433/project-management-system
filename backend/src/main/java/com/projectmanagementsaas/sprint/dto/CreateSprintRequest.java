package com.projectmanagementsaas.sprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreateSprintRequest(
        @NotNull UUID projectId,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 1000) String goal,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
