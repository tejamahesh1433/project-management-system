package com.projectmanagementsaas.board.dto;

import java.util.List;
import java.util.UUID;

public record BoardColumnResponse(
        UUID id,
        UUID boardId,
        String name,
        int position,
        List<BoardTaskResponse> tasks
) {
}
