package com.projectmanagementsaas.project.dto;

import com.projectmanagementsaas.project.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectMemberRoleRequest(@NotNull ProjectRole role) {
}
