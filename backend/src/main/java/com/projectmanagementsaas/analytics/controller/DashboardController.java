package com.projectmanagementsaas.analytics.controller;

import com.projectmanagementsaas.analytics.dto.CreateDashboardRequest;
import com.projectmanagementsaas.analytics.dto.CreateWidgetRequest;
import com.projectmanagementsaas.analytics.dto.DashboardResponse;
import com.projectmanagementsaas.analytics.dto.DashboardWidgetResponse;
import com.projectmanagementsaas.analytics.dto.UpdateDashboardRequest;
import com.projectmanagementsaas.analytics.dto.UpdateWidgetRequest;
import com.projectmanagementsaas.analytics.service.DashboardService;
import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PostMapping
    ResponseEntity<DashboardResponse> create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateDashboardRequest request
    ) {
        return ResponseEntity.ok(dashboardService.create(currentUser.id(), request));
    }

    @GetMapping
    ResponseEntity<List<DashboardResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam UUID workspaceId
    ) {
        return ResponseEntity.ok(dashboardService.list(currentUser.id(), workspaceId));
    }

    @GetMapping("/{dashboardId}")
    ResponseEntity<DashboardResponse> get(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID dashboardId
    ) {
        return ResponseEntity.ok(dashboardService.get(currentUser.id(), dashboardId));
    }

    @PutMapping("/{dashboardId}")
    ResponseEntity<DashboardResponse> update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID dashboardId,
            @Valid @RequestBody UpdateDashboardRequest request
    ) {
        return ResponseEntity.ok(dashboardService.update(currentUser.id(), dashboardId, request));
    }

    @DeleteMapping("/{dashboardId}")
    ResponseEntity<MessageResponse> delete(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID dashboardId
    ) {
        dashboardService.delete(currentUser.id(), dashboardId);
        return ResponseEntity.ok(new MessageResponse("Dashboard deleted"));
    }

    @PostMapping("/{dashboardId}/widgets")
    ResponseEntity<DashboardWidgetResponse> createWidget(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID dashboardId,
            @Valid @RequestBody CreateWidgetRequest request
    ) {
        return ResponseEntity.ok(dashboardService.createWidget(currentUser.id(), dashboardId, request));
    }

    @PutMapping("/{dashboardId}/widgets/{widgetId}")
    ResponseEntity<DashboardWidgetResponse> updateWidget(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID dashboardId,
            @PathVariable UUID widgetId,
            @Valid @RequestBody UpdateWidgetRequest request
    ) {
        return ResponseEntity.ok(dashboardService.updateWidget(currentUser.id(), dashboardId, widgetId, request));
    }

    @DeleteMapping("/{dashboardId}/widgets/{widgetId}")
    ResponseEntity<MessageResponse> deleteWidget(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID dashboardId,
            @PathVariable UUID widgetId
    ) {
        dashboardService.deleteWidget(currentUser.id(), dashboardId, widgetId);
        return ResponseEntity.ok(new MessageResponse("Widget deleted"));
    }
}
