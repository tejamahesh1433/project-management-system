package com.projectmanagementsaas.report.controller;

import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.report.dto.GenerateReportRequest;
import com.projectmanagementsaas.report.dto.ReportResponse;
import com.projectmanagementsaas.report.service.ReportService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/generate")
    ResponseEntity<ReportResponse> generate(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody GenerateReportRequest request
    ) {
        return ResponseEntity.ok(reportService.generate(currentUser.id(), request));
    }

    @GetMapping
    ResponseEntity<List<ReportResponse>> list(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(reportService.list(currentUser.id()));
    }

    @GetMapping("/{id}")
    ResponseEntity<ReportResponse> get(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(reportService.get(currentUser.id(), id));
    }

    @GetMapping("/{id}/export/json")
    ResponseEntity<String> exportJson(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(reportService.exportJson(currentUser.id(), id));
    }

    @GetMapping("/{id}/export/csv")
    ResponseEntity<String> exportCsv(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(reportService.exportCsv(currentUser.id(), id));
    }
}
