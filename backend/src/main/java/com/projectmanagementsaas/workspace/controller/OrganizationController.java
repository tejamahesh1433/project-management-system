package com.projectmanagementsaas.workspace.controller;

import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.workspace.dto.CreateOrganizationRequest;
import com.projectmanagementsaas.workspace.dto.OrganizationResponse;
import com.projectmanagementsaas.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
    private final WorkspaceService workspaceService;

    public OrganizationController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    ResponseEntity<OrganizationResponse> createOrganization(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateOrganizationRequest request
    ) {
        return ResponseEntity.ok(workspaceService.createOrganization(currentUser.id(), request));
    }

    @GetMapping
    ResponseEntity<List<OrganizationResponse>> listOrganizations(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(workspaceService.listOrganizations(currentUser.id()));
    }

    @GetMapping("/{organizationId}")
    ResponseEntity<OrganizationResponse> getOrganization(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID organizationId
    ) {
        return ResponseEntity.ok(workspaceService.getOrganization(currentUser.id(), organizationId));
    }
}
