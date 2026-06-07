package com.projectmanagementsaas.auth.dto;

import com.projectmanagementsaas.role.entity.RoleName;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        Set<RoleName> roles
) {
}
