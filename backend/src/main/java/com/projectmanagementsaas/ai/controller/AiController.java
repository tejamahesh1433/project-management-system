package com.projectmanagementsaas.ai.controller;

import com.projectmanagementsaas.ai.dto.*;
import com.projectmanagementsaas.ai.service.AiAssistantService;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private final AiAssistantService aiAssistantService;

    public AiController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    ResponseEntity<AiChatResponse> chat(@AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiAssistantService.chat(currentUser.id(), request));
    }

    @GetMapping("/conversations")
    ResponseEntity<List<AiConversationResponse>> conversations(@AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam UUID workspaceId) {
        return ResponseEntity.ok(aiAssistantService.conversations(currentUser.id(), workspaceId));
    }

    @PostMapping("/summarize/project/{id}")
    ResponseEntity<AiSummaryResponse> summarizeProject(@AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(aiAssistantService.summarizeProject(currentUser.id(), id));
    }

    @PostMapping("/summarize/sprint/{id}")
    ResponseEntity<AiSummaryResponse> summarizeSprint(@AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(aiAssistantService.summarizeSprint(currentUser.id(), id));
    }

    @PostMapping("/summarize/workspace/{id}")
    ResponseEntity<AiSummaryResponse> summarizeWorkspace(@AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(aiAssistantService.summarizeWorkspace(currentUser.id(), id));
    }

    @PostMapping("/search")
    ResponseEntity<AiSearchResponse> search(@AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody AiSearchRequest request) {
        return ResponseEntity.ok(aiAssistantService.search(currentUser.id(), request));
    }
}
