package com.projectmanagementsaas.analytics.controller;

import com.projectmanagementsaas.analytics.dto.ProjectAnalyticsResponse;
import com.projectmanagementsaas.analytics.dto.SprintAnalyticsResponse;
import com.projectmanagementsaas.analytics.dto.TeamAnalyticsResponse;
import com.projectmanagementsaas.analytics.dto.WorkspaceAnalyticsResponse;
import com.projectmanagementsaas.analytics.service.AnalyticsService;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/workspaces/{workspaceId}")
    ResponseEntity<WorkspaceAnalyticsResponse> workspace(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(analyticsService.workspace(currentUser.id(), workspaceId));
    }

    @GetMapping("/projects/{projectId}")
    ResponseEntity<ProjectAnalyticsResponse> project(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(analyticsService.project(currentUser.id(), projectId));
    }

    @GetMapping("/sprints/{sprintId}")
    ResponseEntity<SprintAnalyticsResponse> sprint(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId
    ) {
        return ResponseEntity.ok(analyticsService.sprint(currentUser.id(), sprintId));
    }

    @GetMapping("/teams/projects/{projectId}")
    ResponseEntity<TeamAnalyticsResponse> team(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(analyticsService.team(currentUser.id(), projectId));
    }
}
