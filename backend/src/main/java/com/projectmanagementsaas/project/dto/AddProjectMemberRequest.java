package com.projectmanagementsaas.project.dto;

import com.projectmanagementsaas.project.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddProjectMemberRequest(
        @NotNull UUID userId,
        @NotNull ProjectRole role
) {
}
