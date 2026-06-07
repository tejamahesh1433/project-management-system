package com.projectmanagementsaas.events.model;

import com.projectmanagementsaas.report.entity.ReportType;
import java.util.UUID;

public record ReportExportedEvent(UUID reportId, ReportType type, String format, UUID actorId) {
}
