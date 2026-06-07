package com.projectmanagementsaas.integration.controller;

import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.integration.dto.CreateIntegrationRequest;
import com.projectmanagementsaas.integration.dto.IntegrationResponse;
import com.projectmanagementsaas.integration.dto.IntegrationTestResponse;
import com.projectmanagementsaas.integration.service.IntegrationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {
    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping
    ResponseEntity<IntegrationResponse> create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateIntegrationRequest request
    ) {
        return ResponseEntity.ok(integrationService.create(currentUser.id(), request));
    }

    @GetMapping
    ResponseEntity<List<IntegrationResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam UUID workspaceId
    ) {
        return ResponseEntity.ok(integrationService.list(currentUser.id(), workspaceId));
    }

    @GetMapping("/{id}")
    ResponseEntity<IntegrationResponse> get(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(integrationService.get(currentUser.id(), id));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<MessageResponse> delete(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        integrationService.delete(currentUser.id(), id);
        return ResponseEntity.ok(new MessageResponse("Integration deleted"));
    }

    @PostMapping("/{id}/test")
    ResponseEntity<IntegrationTestResponse> test(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(integrationService.test(currentUser.id(), id));
    }
}
