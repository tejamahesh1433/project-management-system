package com.projectmanagementsaas.document.controller;

import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.document.dto.*;
import com.projectmanagementsaas.document.service.DocumentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/folders")
    ResponseEntity<FolderResponse> createFolder(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateFolderRequest request) {
        return ResponseEntity.ok(documentService.createFolder(user.id(), request));
    }

    @GetMapping("/folders")
    ResponseEntity<List<FolderResponse>> listFolders(@AuthenticationPrincipal AuthenticatedUser user, @RequestParam UUID projectId) {
        return ResponseEntity.ok(documentService.listFolders(user.id(), projectId));
    }

    @PutMapping("/folders/{folderId}")
    ResponseEntity<FolderResponse> updateFolder(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID folderId,
            @Valid @RequestBody UpdateFolderRequest request) {
        return ResponseEntity.ok(documentService.updateFolder(user.id(), folderId, request));
    }

    @DeleteMapping("/folders/{folderId}")
    ResponseEntity<MessageResponse> deleteFolder(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID folderId) {
        documentService.deleteFolder(user.id(), folderId);
        return ResponseEntity.ok(new MessageResponse("Folder deleted"));
    }

    @PostMapping("/documents")
    ResponseEntity<DocumentResponse> createDocument(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateDocumentRequest request) {
        return ResponseEntity.ok(documentService.createDocument(user.id(), request));
    }

    @GetMapping("/documents")
    ResponseEntity<List<DocumentResponse>> listDocuments(@AuthenticationPrincipal AuthenticatedUser user, @RequestParam UUID projectId) {
        return ResponseEntity.ok(documentService.listDocuments(user.id(), projectId));
    }

    @GetMapping("/documents/{documentId}")
    ResponseEntity<DocumentResponse> getDocument(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID documentId) {
        return ResponseEntity.ok(documentService.getDocument(user.id(), documentId));
    }

    @PutMapping("/documents/{documentId}")
    ResponseEntity<DocumentResponse> updateDocument(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID documentId,
            @Valid @RequestBody UpdateDocumentRequest request) {
        return ResponseEntity.ok(documentService.updateDocument(user.id(), documentId, request));
    }

    @DeleteMapping("/documents/{documentId}")
    ResponseEntity<MessageResponse> deleteDocument(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID documentId) {
        documentService.deleteDocument(user.id(), documentId);
        return ResponseEntity.ok(new MessageResponse("Document deleted"));
    }

    @GetMapping("/documents/{documentId}/versions")
    ResponseEntity<List<DocumentVersionResponse>> versions(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID documentId) {
        return ResponseEntity.ok(documentService.versions(user.id(), documentId));
    }

    @PostMapping("/documents/{documentId}/versions/{versionNumber}/restore")
    ResponseEntity<DocumentResponse> restore(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID documentId,
            @PathVariable int versionNumber) {
        return ResponseEntity.ok(documentService.restoreVersion(user.id(), documentId, versionNumber));
    }
}
