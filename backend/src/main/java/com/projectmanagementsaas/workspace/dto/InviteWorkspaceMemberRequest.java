package com.projectmanagementsaas.workspace.dto;

import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteWorkspaceMemberRequest(
        @Email @NotBlank String email,
        @NotNull WorkspaceRole role
) {
}
