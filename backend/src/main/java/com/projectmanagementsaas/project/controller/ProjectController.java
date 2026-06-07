package com.projectmanagementsaas.project.controller;

import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.project.dto.AddProjectMemberRequest;
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.project.dto.ProjectMemberResponse;
import com.projectmanagementsaas.project.dto.ProjectResponse;
import com.projectmanagementsaas.project.dto.UpdateProjectMemberRoleRequest;
import com.projectmanagementsaas.project.dto.UpdateProjectRequest;
import com.projectmanagementsaas.project.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.createProject(currentUser.id(), request));
    }

    @GetMapping
    ResponseEntity<List<ProjectResponse>> listProjects(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam UUID workspaceId
    ) {
        return ResponseEntity.ok(projectService.listProjects(currentUser.id(), workspaceId));
    }

    @GetMapping("/{projectId}")
    ResponseEntity<ProjectResponse> getProject(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(projectService.getProject(currentUser.id(), projectId));
    }

    @PutMapping("/{projectId}")
    ResponseEntity<ProjectResponse> updateProject(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(currentUser.id(), projectId, request));
    }

    @DeleteMapping("/{projectId}")
    ResponseEntity<MessageResponse> deleteProject(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId
    ) {
        projectService.softDeleteProject(currentUser.id(), projectId);
        return ResponseEntity.ok(new MessageResponse("Project deleted"));
    }

    @PostMapping("/{projectId}/archive")
    ResponseEntity<ProjectResponse> archiveProject(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(projectService.archiveProject(currentUser.id(), projectId));
    }

    @PostMapping("/{projectId}/restore")
    ResponseEntity<ProjectResponse> restoreProject(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(projectService.restoreProject(currentUser.id(), projectId));
    }

    @GetMapping("/{projectId}/members")
    ResponseEntity<List<ProjectMemberResponse>> listMembers(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(projectService.listMembers(currentUser.id(), projectId));
    }

    @PostMapping("/{projectId}/members")
    ResponseEntity<ProjectMemberResponse> addMember(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request
    ) {
        return ResponseEntity.ok(projectService.addMember(currentUser.id(), projectId, request));
    }

    @PatchMapping("/{projectId}/members/{memberId}/role")
    ResponseEntity<ProjectMemberResponse> updateMemberRole(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateProjectMemberRoleRequest request
    ) {
        return ResponseEntity.ok(projectService.updateMemberRole(currentUser.id(), projectId, memberId, request));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    ResponseEntity<MessageResponse> removeMember(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID projectId,
            @PathVariable UUID memberId
    ) {
        projectService.removeMember(currentUser.id(), projectId, memberId);
        return ResponseEntity.ok(new MessageResponse("Project member removed"));
    }
}
