package com.projectmanagementsaas.report.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.projectmanagementsaas.report.entity.ReportType;
import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        ReportType type,
        String title,
        UUID workspaceId,
        UUID projectId,
        UUID sprintId,
        UUID generatedById,
        Instant generatedAt,
        JsonNode metrics
) {
}
