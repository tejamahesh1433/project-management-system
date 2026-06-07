package com.projectmanagementsaas.sprint.dto;

import com.projectmanagementsaas.sprint.entity.SprintStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SprintResponse(
        UUID id,
        UUID projectId,
        String name,
        String goal,
        SprintStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt
) {
}
