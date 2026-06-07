package com.projectmanagementsaas.report.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record ReportSnapshotResponse(
        UUID id,
        UUID reportId,
        Instant createdAt,
        JsonNode metrics
) {
}
