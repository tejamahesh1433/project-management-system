package com.projectmanagementsaas.board.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MoveTaskRequest(
        @NotNull UUID taskId,
        @NotNull UUID columnId,
        @Min(0) int position
) {
}
