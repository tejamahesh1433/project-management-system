package com.projectmanagementsaas.sprint.controller;

import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.sprint.dto.AddSprintTaskRequest;
import com.projectmanagementsaas.sprint.dto.CreateSprintRequest;
import com.projectmanagementsaas.sprint.dto.SprintMetricsResponse;
import com.projectmanagementsaas.sprint.dto.SprintResponse;
import com.projectmanagementsaas.sprint.dto.SprintTaskResponse;
import com.projectmanagementsaas.sprint.dto.UpdateSprintRequest;
import com.projectmanagementsaas.sprint.service.SprintService;
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
@RequestMapping("/api/v1/sprints")
public class SprintController {
    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    ResponseEntity<SprintResponse> createSprint(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateSprintRequest request
    ) {
        return ResponseEntity.ok(sprintService.createSprint(currentUser.id(), request));
    }

    @GetMapping
    ResponseEntity<List<SprintResponse>> listSprints(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam UUID projectId
    ) {
        return ResponseEntity.ok(sprintService.listSprints(currentUser.id(), projectId));
    }

    @GetMapping("/{sprintId}")
    ResponseEntity<SprintResponse> getSprint(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId
    ) {
        return ResponseEntity.ok(sprintService.getSprint(currentUser.id(), sprintId));
    }

    @PutMapping("/{sprintId}")
    ResponseEntity<SprintResponse> updateSprint(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId,
            @Valid @RequestBody UpdateSprintRequest request
    ) {
        return ResponseEntity.ok(sprintService.updateSprint(currentUser.id(), sprintId, request));
    }

    @DeleteMapping("/{sprintId}")
    ResponseEntity<MessageResponse> deleteSprint(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId
    ) {
        sprintService.deleteSprint(currentUser.id(), sprintId);
        return ResponseEntity.ok(new MessageResponse("Sprint deleted"));
    }

    @PostMapping("/{sprintId}/start")
    ResponseEntity<SprintResponse> startSprint(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId
    ) {
        return ResponseEntity.ok(sprintService.startSprint(currentUser.id(), sprintId));
    }

    @PostMapping("/{sprintId}/complete")
    ResponseEntity<SprintResponse> completeSprint(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId
    ) {
        return ResponseEntity.ok(sprintService.completeSprint(currentUser.id(), sprintId));
    }

    @PostMapping("/{sprintId}/cancel")
    ResponseEntity<SprintResponse> cancelSprint(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId
    ) {
        return ResponseEntity.ok(sprintService.cancelSprint(currentUser.id(), sprintId));
    }

    @PostMapping("/{sprintId}/tasks")
    ResponseEntity<SprintTaskResponse> addTask(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId,
            @Valid @RequestBody AddSprintTaskRequest request
    ) {
        return ResponseEntity.ok(sprintService.addTask(currentUser.id(), sprintId, request));
    }

    @DeleteMapping("/{sprintId}/tasks/{taskId}")
    ResponseEntity<MessageResponse> removeTask(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId,
            @PathVariable UUID taskId
    ) {
        sprintService.removeTask(currentUser.id(), sprintId, taskId);
        return ResponseEntity.ok(new MessageResponse("Task removed from sprint"));
    }

    @GetMapping("/{sprintId}/tasks")
    ResponseEntity<List<SprintTaskResponse>> listTasks(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId
    ) {
        return ResponseEntity.ok(sprintService.listTasks(currentUser.id(), sprintId));
    }

    @GetMapping("/{sprintId}/metrics")
    ResponseEntity<SprintMetricsResponse> metrics(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID sprintId
    ) {
        return ResponseEntity.ok(sprintService.metrics(currentUser.id(), sprintId));
    }
}
