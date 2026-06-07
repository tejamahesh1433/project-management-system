package com.projectmanagementsaas.audit.controller;

import com.projectmanagementsaas.audit.dto.AuditLogResponse;
import com.projectmanagementsaas.audit.service.AuditLogService;
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
@RequestMapping("/api/v1/audit")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    ResponseEntity<List<AuditLogResponse>> userAudit(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(auditLogService.userAudit(user.id()));
    }

    @GetMapping("/{entityType}/{entityId}")
    ResponseEntity<List<AuditLogResponse>> entityAudit(@PathVariable String entityType, @PathVariable UUID entityId) {
        return ResponseEntity.ok(auditLogService.entityAudit(entityType, entityId));
    }
}
