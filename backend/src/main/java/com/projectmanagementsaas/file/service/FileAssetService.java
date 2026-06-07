package com.projectmanagementsaas.file.service;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.document.entity.Folder;
import com.projectmanagementsaas.document.repository.FolderRepository;
import com.projectmanagementsaas.events.model.FileDeletedEvent;
import com.projectmanagementsaas.events.model.FileUploadedEvent;
import com.projectmanagementsaas.file.dto.CreateFileAssetRequest;
import com.projectmanagementsaas.file.dto.FileAssetResponse;
import com.projectmanagementsaas.file.entity.FileAsset;
import com.projectmanagementsaas.file.mapper.FileAssetMapper;
import com.projectmanagementsaas.file.repository.FileAssetRepository;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.task.service.TaskAccessService;
import com.projectmanagementsaas.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileAssetService {
    private final FileAssetRepository fileAssetRepository;
    private final FolderRepository folderRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskAccessService taskAccessService;
    private final FileAssetMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public FileAssetService(FileAssetRepository fileAssetRepository, FolderRepository folderRepository,
            ProjectRepository projectRepository, UserRepository userRepository, TaskAccessService taskAccessService,
            FileAssetMapper mapper, ApplicationEventPublisher eventPublisher) {
        this.fileAssetRepository = fileAssetRepository;
        this.folderRepository = folderRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskAccessService = taskAccessService;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public FileAssetResponse create(UUID userId, CreateFileAssetRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));
        taskAccessService.requireContributor(project.getId(), userId);
        Folder folder = request.folderId() == null ? null : folderRepository.findByIdAndDeletedAtIsNull(request.folderId())
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        if (folder != null && !folder.getProject().getId().equals(project.getId())) {
            throw new BadRequestException("Folder must belong to the same project");
        }

        FileAsset file = new FileAsset();
        file.setProject(project);
        file.setFolder(folder);
        file.setFileName(request.fileName().trim());
        file.setStoragePath("storage/uploads/" + project.getId() + "/" + UUID.randomUUID() + "-" + request.fileName().trim());
        file.setContentType(request.contentType());
        file.setSizeBytes(request.sizeBytes());
        file.setUploadedBy(userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found")));
        FileAsset saved = fileAssetRepository.save(file);
        eventPublisher.publishEvent(new FileUploadedEvent(saved.getId(), project.getId(), userId));
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FileAssetResponse> list(UUID userId, UUID projectId) {
        taskAccessService.requireReadableTaskForProject(projectId, userId);
        return fileAssetRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FileAssetResponse get(UUID userId, UUID fileId) {
        FileAsset file = requireFile(fileId);
        taskAccessService.requireReadableTaskForProject(file.getProject().getId(), userId);
        return mapper.toResponse(file);
    }

    @Transactional
    public void delete(UUID userId, UUID fileId) {
        FileAsset file = requireFile(fileId);
        taskAccessService.requireContributor(file.getProject().getId(), userId);
        file.softDelete();
        fileAssetRepository.save(file);
        eventPublisher.publishEvent(new FileDeletedEvent(fileId, file.getProject().getId(), userId));
    }

    private FileAsset requireFile(UUID fileId) {
        return fileAssetRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new NotFoundException("File asset not found"));
    }
}
