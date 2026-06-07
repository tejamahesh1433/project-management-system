package com.projectmanagementsaas.board.dto;

import java.util.UUID;

public record BoardTaskResponse(
        UUID id,
        UUID taskId,
        String title,
        UUID columnId,
        int position
) {
}
