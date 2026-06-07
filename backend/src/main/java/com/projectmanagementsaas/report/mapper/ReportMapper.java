package com.projectmanagementsaas.report.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagementsaas.report.dto.ReportResponse;
import com.projectmanagementsaas.report.dto.ReportSnapshotResponse;
import com.projectmanagementsaas.report.entity.Report;
import com.projectmanagementsaas.report.entity.ReportSnapshot;
import java.io.UncheckedIOException;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {
    private final ObjectMapper objectMapper;

    public ReportMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReportResponse toResponse(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getType(),
                report.getTitle(),
                report.getWorkspaceId(),
                report.getProjectId(),
                report.getSprintId(),
                report.getGeneratedBy().getId(),
                report.getGeneratedAt(),
                readTree(report.getMetricsJson()));
    }

    public ReportSnapshotResponse toSnapshotResponse(ReportSnapshot snapshot) {
        return new ReportSnapshotResponse(
                snapshot.getId(),
                snapshot.getReport().getId(),
                snapshot.getCreatedAt(),
                readTree(snapshot.getMetricsJson()));
    }

    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (java.io.IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
