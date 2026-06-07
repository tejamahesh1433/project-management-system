package com.projectmanagementsaas.workspace.dto;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(@NotBlank String token) {
}
