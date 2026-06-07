package com.projectmanagementsaas.notification.service;

import com.projectmanagementsaas.document.repository.DocumentRepository;
import com.projectmanagementsaas.events.model.*;
import com.projectmanagementsaas.file.repository.FileAssetRepository;
import com.projectmanagementsaas.notification.entity.NotificationType;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.sprint.repository.SprintRepository;
import com.projectmanagementsaas.task.repository.TaskCommentRepository;
import com.projectmanagementsaas.task.repository.TaskRepository;
import com.projectmanagementsaas.user.repository.UserRepository;
import com.projectmanagementsaas.workspace.repository.WorkspaceRepository;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {
    private final NotificationService notificationService;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final DocumentRepository documentRepository;
    private final FileAssetRepository fileAssetRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public NotificationEventListener(NotificationService notificationService, TaskRepository taskRepository,
            TaskCommentRepository taskCommentRepository, ProjectRepository projectRepository, SprintRepository sprintRepository,
            DocumentRepository documentRepository, FileAssetRepository fileAssetRepository,
            WorkspaceRepository workspaceRepository, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.documentRepository = documentRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    @EventListener
    public void on(TaskAssignedEvent event) {
        taskRepository.findById(event.taskId()).ifPresent(task -> {
            if (task.getAssignee() != null) {
                notify(task.getAssignee().getId(), NotificationType.TASK_ASSIGNED, "Task assigned",
                        "You were assigned to " + task.getTitle(), "TASK", task.getId(),
                        task.getProject().getWorkspace().getId(), task.getProject().getId());
            }
        });
    }

    @EventListener
    public void on(TaskUpdatedEvent event) {
        taskRepository.findById(event.taskId()).ifPresent(task -> {
            if (task.getAssignee() != null) {
                notify(task.getAssignee().getId(), NotificationType.TASK_UPDATED, "Task updated",
                        task.getTitle() + " was updated", "TASK", task.getId(),
                        task.getProject().getWorkspace().getId(), task.getProject().getId());
            }
        });
    }

    @EventListener
    public void on(TaskCommentCreatedEvent event) {
        taskRepository.findById(event.taskId()).ifPresent(task -> {
            UUID recipient = task.getAssignee() == null ? task.getCreatedBy().getId() : task.getAssignee().getId();
            notify(recipient, NotificationType.COMMENT_ADDED, "Comment added",
                    "A comment was added to " + task.getTitle(), "TASK", task.getId(),
                    task.getProject().getWorkspace().getId(), task.getProject().getId());
        });
    }

    @EventListener
    public void on(ProjectCreatedEvent event) {
        notify(event.actorId(), NotificationType.PROJECT_CREATED, "Project created",
                "Project was created", "PROJECT", event.projectId(), event.workspaceId(), event.projectId());
    }

    @EventListener
    public void on(SprintStartedEvent event) {
        sprintRepository.findById(event.sprintId()).ifPresent(sprint ->
                notify(event.actorId(), NotificationType.SPRINT_STARTED, "Sprint started",
                        sprint.getName() + " started", "SPRINT", sprint.getId(),
                        sprint.getProject().getWorkspace().getId(), sprint.getProject().getId()));
    }

    @EventListener
    public void on(SprintCompletedEvent event) {
        sprintRepository.findById(event.sprintId()).ifPresent(sprint ->
                notify(event.actorId(), NotificationType.SPRINT_COMPLETED, "Sprint completed",
                        sprint.getName() + " completed", "SPRINT", sprint.getId(),
                        sprint.getProject().getWorkspace().getId(), sprint.getProject().getId()));
    }

    @EventListener
    public void on(DocumentUpdatedEvent event) {
        documentRepository.findById(event.documentId()).ifPresent(document ->
                notify(document.getCreatedBy().getId(), NotificationType.DOCUMENT_UPDATED, "Document updated",
                        document.getTitle() + " was updated", "DOCUMENT", document.getId(),
                        document.getProject().getWorkspace().getId(), document.getProject().getId()));
    }

    @EventListener
    public void on(FileUploadedEvent event) {
        fileAssetRepository.findById(event.fileId()).ifPresent(file ->
                notify(file.getUploadedBy().getId(), NotificationType.FILE_UPLOADED, "File uploaded",
                        file.getFileName() + " was uploaded", "FILE", file.getId(),
                        file.getProject().getWorkspace().getId(), file.getProject().getId()));
    }

    @EventListener
    public void on(WorkspaceInvitationCreatedEvent event) {
        userRepository.findByEmailIgnoreCase(event.email()).ifPresent(user ->
                workspaceRepository.findById(event.workspaceId()).ifPresent(workspace ->
                        notify(user.getId(), NotificationType.WORKSPACE_INVITATION, "Workspace invitation",
                                "You were invited to " + workspace.getName(), "WORKSPACE", workspace.getId(),
                                workspace.getId(), null)));
    }

    private void notify(UUID userId, NotificationType type, String title, String message, String entityType,
            UUID entityId, UUID workspaceId, UUID projectId) {
        notificationService.create(userId, type, title, message, entityType, entityId, workspaceId, projectId);
    }
}
