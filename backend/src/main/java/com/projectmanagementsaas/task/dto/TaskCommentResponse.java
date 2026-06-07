package com.projectmanagementsaas.task.dto;

import java.time.Instant;
import java.util.UUID;

public record TaskCommentResponse(
        UUID id,
        UUID taskId,
        UUID authorId,
        String authorEmail,
        String body,
        Instant createdAt,
        Instant updatedAt
) {
}
