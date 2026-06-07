package com.projectmanagementsaas.backup.controller;

import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.backup.dto.BackupResponse;
import com.projectmanagementsaas.backup.service.BackupService;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backups")
public class BackupController {
    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    ResponseEntity<BackupResponse> create(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(backupService.create(currentUser.id()));
    }

    @GetMapping
    ResponseEntity<List<BackupResponse>> history() {
        return ResponseEntity.ok(backupService.history());
    }

    @PostMapping("/{id}/restore")
    ResponseEntity<BackupResponse> restore(@PathVariable UUID id) {
        return ResponseEntity.ok(backupService.restore(id));
    }

    @GetMapping("/{id}/download")
    ResponseEntity<Resource> download(@PathVariable UUID id) {
        Path path = backupService.downloadPath(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
                .body(new FileSystemResource(path));
    }
}
