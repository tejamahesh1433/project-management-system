package com.projectmanagementsaas.board.dto;

import com.projectmanagementsaas.board.entity.BoardTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateBoardRequest(
        @NotNull UUID projectId,
        @NotBlank @Size(max = 160) String name,
        @NotNull BoardTemplate template
) {
}
