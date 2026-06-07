package com.projectmanagementsaas.file.controller;

import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.file.dto.CreateFileAssetRequest;
import com.projectmanagementsaas.file.dto.FileAssetResponse;
import com.projectmanagementsaas.file.service.FileAssetService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/files")
public class FileAssetController {
    private final FileAssetService fileAssetService;

    public FileAssetController(FileAssetService fileAssetService) {
        this.fileAssetService = fileAssetService;
    }

    @PostMapping
    ResponseEntity<FileAssetResponse> create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateFileAssetRequest request) {
        return ResponseEntity.ok(fileAssetService.create(user.id(), request));
    }

    @GetMapping
    ResponseEntity<List<FileAssetResponse>> list(@AuthenticationPrincipal AuthenticatedUser user, @RequestParam UUID projectId) {
        return ResponseEntity.ok(fileAssetService.list(user.id(), projectId));
    }

    @GetMapping("/{fileId}")
    ResponseEntity<FileAssetResponse> get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID fileId) {
        return ResponseEntity.ok(fileAssetService.get(user.id(), fileId));
    }

    @DeleteMapping("/{fileId}")
    ResponseEntity<MessageResponse> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID fileId) {
        fileAssetService.delete(user.id(), fileId);
        return ResponseEntity.ok(new MessageResponse("File deleted"));
    }
}
