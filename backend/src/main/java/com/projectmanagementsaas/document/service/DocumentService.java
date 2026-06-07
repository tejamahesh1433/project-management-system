package com.projectmanagementsaas.document.service;

import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.document.dto.*;
import com.projectmanagementsaas.document.entity.*;
import com.projectmanagementsaas.document.mapper.DocumentMapper;
import com.projectmanagementsaas.document.repository.*;
import com.projectmanagementsaas.document.validator.FolderHierarchyValidator;
import com.projectmanagementsaas.events.model.*;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.task.service.TaskAccessService;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskAccessService taskAccessService;
    private final FolderHierarchyValidator folderHierarchyValidator;
    private final DocumentMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public DocumentService(FolderRepository folderRepository, DocumentRepository documentRepository,
            DocumentVersionRepository versionRepository, ProjectRepository projectRepository, UserRepository userRepository,
            TaskAccessService taskAccessService, FolderHierarchyValidator folderHierarchyValidator,
            DocumentMapper mapper, ApplicationEventPublisher eventPublisher) {
        this.folderRepository = folderRepository;
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskAccessService = taskAccessService;
        this.folderHierarchyValidator = folderHierarchyValidator;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public FolderResponse createFolder(UUID userId, CreateFolderRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));
        taskAccessService.requireContributor(project.getId(), userId);
        Folder parent = resolveFolder(request.parentFolderId());
        folderHierarchyValidator.validateParent(parent, project.getId(), null);
        Folder folder = new Folder();
        folder.setProject(project);
        folder.setParentFolder(parent);
        folder.setName(request.name().trim());
        return mapper.toFolderResponse(folderRepository.save(folder));
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> listFolders(UUID userId, UUID projectId) {
        taskAccessService.requireReadableTaskForProject(projectId, userId);
        return folderRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).stream()
                .map(mapper::toFolderResponse).toList();
    }

    @Transactional
    public FolderResponse updateFolder(UUID userId, UUID folderId, UpdateFolderRequest request) {
        Folder folder = requireFolder(folderId);
        taskAccessService.requireContributor(folder.getProject().getId(), userId);
        Folder parent = resolveFolder(request.parentFolderId());
        folderHierarchyValidator.validateParent(parent, folder.getProject().getId(), folder.getId());
        folder.setParentFolder(parent);
        folder.setName(request.name().trim());
        folder.setUpdatedAt(Instant.now());
        return mapper.toFolderResponse(folderRepository.save(folder));
    }

    @Transactional
    public void deleteFolder(UUID userId, UUID folderId) {
        Folder folder = requireFolder(folderId);
        taskAccessService.requireContributor(folder.getProject().getId(), userId);
        folder.softDelete();
        folderRepository.save(folder);
    }

    @Transactional
    public DocumentResponse createDocument(UUID userId, CreateDocumentRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));
        taskAccessService.requireContributor(project.getId(), userId);
        Folder folder = resolveFolder(request.folderId());
        validateFolderProject(folder, project.getId());
        Document document = new Document();
        document.setProject(project);
        document.setFolder(folder);
        document.setTitle(request.title().trim());
        document.setContent(request.content());
        document.setCreatedBy(getUser(userId));
        Document saved = documentRepository.save(document);
        createVersion(saved, userId);
        eventPublisher.publishEvent(new DocumentCreatedEvent(saved.getId(), project.getId(), userId));
        return mapper.toDocumentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listDocuments(UUID userId, UUID projectId) {
        taskAccessService.requireReadableTaskForProject(projectId, userId);
        return documentRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).stream()
                .map(mapper::toDocumentResponse).toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(UUID userId, UUID documentId) {
        Document document = requireDocument(documentId);
        taskAccessService.requireReadableTaskForProject(document.getProject().getId(), userId);
        return mapper.toDocumentResponse(document);
    }

    @Transactional
    public DocumentResponse updateDocument(UUID userId, UUID documentId, UpdateDocumentRequest request) {
        Document document = requireDocument(documentId);
        taskAccessService.requireContributor(document.getProject().getId(), userId);
        Folder folder = resolveFolder(request.folderId());
        validateFolderProject(folder, document.getProject().getId());
        document.setFolder(folder);
        document.setTitle(request.title().trim());
        document.setContent(request.content());
        document.setStatus(request.status() == null ? document.getStatus() : request.status());
        document.setCurrentVersion(document.getCurrentVersion() + 1);
        document.setUpdatedAt(Instant.now());
        Document saved = documentRepository.save(document);
        createVersion(saved, userId);
        eventPublisher.publishEvent(new DocumentUpdatedEvent(documentId, document.getProject().getId(), userId));
        if (saved.getStatus() == DocumentStatus.PUBLISHED) {
            eventPublisher.publishEvent(new DocumentPublishedEvent(documentId, document.getProject().getId(), userId));
        }
        return mapper.toDocumentResponse(saved);
    }

    @Transactional
    public void deleteDocument(UUID userId, UUID documentId) {
        Document document = requireDocument(documentId);
        taskAccessService.requireContributor(document.getProject().getId(), userId);
        document.softDelete();
        documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> versions(UUID userId, UUID documentId) {
        Document document = requireDocument(documentId);
        taskAccessService.requireReadableTaskForProject(document.getProject().getId(), userId);
        return versionRepository.findByDocument_IdOrderByVersionNumberDesc(documentId).stream()
                .map(mapper::toVersionResponse).toList();
    }

    @Transactional
    public DocumentResponse restoreVersion(UUID userId, UUID documentId, int versionNumber) {
        Document document = requireDocument(documentId);
        taskAccessService.requireContributor(document.getProject().getId(), userId);
        DocumentVersion version = versionRepository.findByDocument_IdAndVersionNumber(documentId, versionNumber)
                .orElseThrow(() -> new NotFoundException("Document version not found"));
        document.setTitle(version.getTitle());
        document.setContent(version.getContent());
        document.setCurrentVersion(document.getCurrentVersion() + 1);
        document.setUpdatedAt(Instant.now());
        Document saved = documentRepository.save(document);
        createVersion(saved, userId);
        return mapper.toDocumentResponse(saved);
    }

    private void createVersion(Document document, UUID userId) {
        DocumentVersion version = new DocumentVersion();
        version.setDocument(document);
        version.setVersionNumber(document.getCurrentVersion());
        version.setTitle(document.getTitle());
        version.setContent(document.getContent());
        version.setCreatedBy(getUser(userId));
        versionRepository.save(version);
        eventPublisher.publishEvent(new DocumentVersionCreatedEvent(document.getId(), document.getCurrentVersion(), userId));
    }

    private Folder resolveFolder(UUID folderId) {
        return folderId == null ? null : requireFolder(folderId);
    }

    private Folder requireFolder(UUID folderId) {
        return folderRepository.findByIdAndDeletedAtIsNull(folderId)
                .orElseThrow(() -> new NotFoundException("Folder not found"));
    }

    private Document requireDocument(UUID documentId) {
        return documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }

    private void validateFolderProject(Folder folder, UUID projectId) {
        if (folder != null && !folder.getProject().getId().equals(projectId)) {
            throw new com.projectmanagementsaas.common.exception.BadRequestException("Folder must belong to the same project");
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }
}
