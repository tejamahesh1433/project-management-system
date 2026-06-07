package com.projectmanagementsaas.task.dto;

import java.time.Instant;
import java.util.UUID;

public record LabelResponse(
        UUID id,
        UUID projectId,
        String name,
        String color,
        Instant createdAt
) {
}
