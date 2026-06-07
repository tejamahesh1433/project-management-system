package com.projectmanagementsaas.project.dto;

import com.projectmanagementsaas.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 120) String slug,
        @Size(max = 1000) String description,
        ProjectStatus status,
        @Size(max = 32) String color,
        @Size(max = 80) String icon
) {
}
