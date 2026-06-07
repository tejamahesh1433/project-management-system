package com.projectmanagementsaas.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBoardRequest(@NotBlank @Size(max = 160) String name) {
}
