package com.projectmanagementsaas.workspace.dto;

import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import jakarta.validation.constraints.NotNull;

public record UpdateWorkspaceMemberRoleRequest(@NotNull WorkspaceRole role) {
}
