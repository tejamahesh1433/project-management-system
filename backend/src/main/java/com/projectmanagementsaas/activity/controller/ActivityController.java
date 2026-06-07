package com.projectmanagementsaas.activity.controller;

import com.projectmanagementsaas.activity.dto.ActivityResponse;
import com.projectmanagementsaas.activity.service.ActivityService;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    ResponseEntity<List<ActivityResponse>> userTimeline(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(activityService.userTimeline(user.id()));
    }

    @GetMapping("/workspaces/{workspaceId}")
    ResponseEntity<List<ActivityResponse>> workspaceFeed(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(activityService.workspaceFeed(user.id(), workspaceId));
    }

    @GetMapping("/projects/{projectId}")
    ResponseEntity<List<ActivityResponse>> projectFeed(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID projectId) {
        return ResponseEntity.ok(activityService.projectFeed(user.id(), projectId));
    }
}
