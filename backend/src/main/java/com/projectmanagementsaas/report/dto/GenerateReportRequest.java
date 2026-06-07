package com.projectmanagementsaas.report.dto;

import com.projectmanagementsaas.report.entity.ReportType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GenerateReportRequest(
        @NotNull ReportType type,
        UUID workspaceId,
        UUID projectId,
        UUID sprintId
) {
}
