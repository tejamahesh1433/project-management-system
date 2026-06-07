package com.projectmanagementsaas.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDashboardRequest(
        @NotBlank @Size(max = 160) String name
) {
}
