package com.projectmanagementsaas.notification.controller;

import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.notification.dto.*;
import com.projectmanagementsaas.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(notificationService.list(user.id()));
    }

    @GetMapping("/notifications/unread")
    ResponseEntity<UnreadCountResponse> unread(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(notificationService.unread(user.id()));
    }

    @PatchMapping("/notifications/{id}/read")
    ResponseEntity<NotificationResponse> markRead(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markRead(user.id(), id));
    }

    @PatchMapping("/notifications/read-all")
    ResponseEntity<MessageResponse> markAllRead(@AuthenticationPrincipal AuthenticatedUser user) {
        notificationService.markAllRead(user.id());
        return ResponseEntity.ok(new MessageResponse("All notifications marked read"));
    }

    @DeleteMapping("/notifications/{id}")
    ResponseEntity<MessageResponse> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        notificationService.delete(user.id(), id);
        return ResponseEntity.ok(new MessageResponse("Notification deleted"));
    }

    @GetMapping("/notification-preferences")
    ResponseEntity<List<NotificationPreferenceItem>> preferences(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(notificationService.preferences(user.id()));
    }

    @PutMapping("/notification-preferences")
    ResponseEntity<List<NotificationPreferenceItem>> updatePreferences(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody NotificationPreferencesRequest request
    ) {
        return ResponseEntity.ok(notificationService.updatePreferences(user.id(), request));
    }
}
