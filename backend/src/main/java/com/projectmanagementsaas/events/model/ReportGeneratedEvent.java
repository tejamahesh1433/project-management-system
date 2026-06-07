package com.projectmanagementsaas.events.model;

import com.projectmanagementsaas.report.entity.ReportType;
import java.util.UUID;

public record ReportGeneratedEvent(UUID reportId, ReportType type, UUID actorId) {
}
