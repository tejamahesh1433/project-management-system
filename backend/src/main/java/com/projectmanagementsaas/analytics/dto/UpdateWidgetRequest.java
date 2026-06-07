package com.projectmanagementsaas.analytics.dto;

import com.projectmanagementsaas.analytics.entity.WidgetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateWidgetRequest(
        @NotNull WidgetType type,
        @NotBlank @Size(max = 160) String title,
        @Min(0) Integer position,
        String configJson
) {
}
