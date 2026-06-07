package com.projectmanagementsaas.board.dto;

import com.projectmanagementsaas.board.entity.BoardTemplate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BoardResponse(
        UUID id,
        UUID projectId,
        String name,
        BoardTemplate template,
        List<BoardColumnResponse> columns,
        Instant createdAt,
        Instant updatedAt
) {
}
