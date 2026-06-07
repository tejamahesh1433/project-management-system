package com.projectmanagementsaas.board.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBoardColumnRequest(
        @NotBlank @Size(max = 120) String name,
        @Min(0) int position
) {
}
