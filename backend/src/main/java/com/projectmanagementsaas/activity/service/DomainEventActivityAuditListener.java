package com.projectmanagementsaas.activity.service;

import com.projectmanagementsaas.audit.service.AuditLogService;
import com.projectmanagementsaas.audit.service.RequestAuditContext;
import com.projectmanagementsaas.events.model.*;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DomainEventActivityAuditListener {
    private final ActivityService activityService;
    private final AuditLogService auditLogService;
    private final EventContextResolver resolver;
    private final RequestAuditContext requestAuditContext;

    public DomainEventActivityAuditListener(ActivityService activityService, AuditLogService auditLogService,
            EventContextResolver resolver, RequestAuditContext requestAuditContext) {
        this.activityService = activityService;
        this.auditLogService = auditLogService;
        this.resolver = resolver;
        this.requestAuditContext = requestAuditContext;
    }

    @EventListener
    public void on(ProjectCreatedEvent event) { record(resolver.forProject(event.projectId()), event.actorId(), "CREATED", "PROJECT", event.projectId(), "Project created", null, event.toString()); }
    @EventListener
    public void on(ProjectUpdatedEvent event) { record(resolver.forProject(event.projectId()), event.actorId(), "UPDATED", "PROJECT", event.projectId(), "Project updated", null, event.toString()); }
    @EventListener
    public void on(ProjectArchivedEvent event) { record(resolver.forProject(event.projectId()), event.actorId(), "ARCHIVED", "PROJECT", event.projectId(), "Project archived", null, event.toString()); }
    @EventListener
    public void on(ProjectRestoredEvent event) { record(resolver.forProject(event.projectId()), event.actorId(), "RESTORED", "PROJECT", event.projectId(), "Project restored", null, event.toString()); }
    @EventListener
    public void on(ProjectMemberAddedEvent event) { record(resolver.forProject(event.projectId()), event.actorId(), "MEMBER_ADDED", "PROJECT", event.projectId(), "Project member added", null, event.toString()); }
    @EventListener
    public void on(ProjectMemberRemovedEvent event) { record(resolver.forProject(event.projectId()), event.actorId(), "MEMBER_REMOVED", "PROJECT", event.projectId(), "Project member removed", event.toString(), null); }

    @EventListener
    public void on(TaskCreatedEvent event) { record(resolver.forTask(event.taskId()), event.actorId(), "CREATED", "TASK", event.taskId(), "Task created", null, event.toString()); }
    @EventListener
    public void on(TaskUpdatedEvent event) { record(resolver.forTask(event.taskId()), event.actorId(), "UPDATED", "TASK", event.taskId(), "Task updated", null, event.toString()); }
    @EventListener
    public void on(TaskDeletedEvent event) { record(resolver.forTask(event.taskId()), event.actorId(), "DELETED", "TASK", event.taskId(), "Task deleted", event.toString(), null); }
    @EventListener
    public void on(TaskAssignedEvent event) { record(resolver.forTask(event.taskId()), event.actorId(), "ASSIGNED", "TASK", event.taskId(), "Task assigned", null, event.toString()); }
    @EventListener
    public void on(TaskStatusChangedEvent event) { record(resolver.forTask(event.taskId()), event.actorId(), "STATUS_CHANGED", "TASK", event.taskId(), "Task status changed", null, event.toString()); }
    @EventListener
    public void on(TaskCommentCreatedEvent event) { record(resolver.forTask(event.taskId()), event.actorId(), "COMMENTED", "TASK", event.taskId(), "Task comment created", null, event.toString()); }
    @EventListener
    public void on(TaskLabelAddedEvent event) { record(resolver.forTask(event.taskId()), event.actorId(), "LABEL_ADDED", "TASK", event.taskId(), "Task label added", null, event.toString()); }

    @EventListener
    public void on(SprintCreatedEvent event) { record(resolver.forSprint(event.sprintId()), event.actorId(), "CREATED", "SPRINT", event.sprintId(), "Sprint created", null, event.toString()); }
    @EventListener
    public void on(SprintUpdatedEvent event) { record(resolver.forSprint(event.sprintId()), event.actorId(), "UPDATED", "SPRINT", event.sprintId(), "Sprint updated", null, event.toString()); }
    @EventListener
    public void on(SprintStartedEvent event) { record(resolver.forSprint(event.sprintId()), event.actorId(), "STARTED", "SPRINT", event.sprintId(), "Sprint started", null, event.toString()); }
    @EventListener
    public void on(SprintCompletedEvent event) { record(resolver.forSprint(event.sprintId()), event.actorId(), "COMPLETED", "SPRINT", event.sprintId(), "Sprint completed", null, event.toString()); }
    @EventListener
    public void on(SprintCancelledEvent event) { record(resolver.forSprint(event.sprintId()), event.actorId(), "CANCELLED", "SPRINT", event.sprintId(), "Sprint cancelled", event.toString(), null); }
    @EventListener
    public void on(SprintTaskAddedEvent event) { record(resolver.forSprint(event.sprintId()), event.actorId(), "TASK_ADDED", "SPRINT", event.sprintId(), "Sprint task added", null, event.toString()); }
    @EventListener
    public void on(SprintTaskRemovedEvent event) { record(resolver.forSprint(event.sprintId()), event.actorId(), "TASK_REMOVED", "SPRINT", event.sprintId(), "Sprint task removed", event.toString(), null); }

    @EventListener
    public void on(DocumentCreatedEvent event) { record(resolver.forDocument(event.documentId()), event.actorId(), "CREATED", "DOCUMENT", event.documentId(), "Document created", null, event.toString()); }
    @EventListener
    public void on(DocumentUpdatedEvent event) { record(resolver.forDocument(event.documentId()), event.actorId(), "UPDATED", "DOCUMENT", event.documentId(), "Document updated", null, event.toString()); }
    @EventListener
    public void on(DocumentPublishedEvent event) { record(resolver.forDocument(event.documentId()), event.actorId(), "PUBLISHED", "DOCUMENT", event.documentId(), "Document published", null, event.toString()); }
    @EventListener
    public void on(DocumentVersionCreatedEvent event) { record(resolver.forDocument(event.documentId()), event.actorId(), "VERSION_CREATED", "DOCUMENT", event.documentId(), "Document version created", null, event.toString()); }

    @EventListener
    public void on(FileUploadedEvent event) { record(resolver.forFile(event.fileId()), event.actorId(), "UPLOADED", "FILE", event.fileId(), "File uploaded", null, event.toString()); }
    @EventListener
    public void on(FileDeletedEvent event) { record(resolver.forFile(event.fileId()), event.actorId(), "DELETED", "FILE", event.fileId(), "File deleted", event.toString(), null); }

    @EventListener
    public void on(BoardCreatedEvent event) { record(resolver.forBoard(event.boardId()), event.actorId(), "CREATED", "BOARD", event.boardId(), "Board created", null, event.toString()); }
    @EventListener
    public void on(BoardUpdatedEvent event) { record(resolver.forBoard(event.boardId()), event.actorId(), "UPDATED", "BOARD", event.boardId(), "Board updated", null, event.toString()); }
    @EventListener
    public void on(BoardDeletedEvent event) { record(resolver.forBoard(event.boardId()), event.actorId(), "DELETED", "BOARD", event.boardId(), "Board deleted", event.toString(), null); }
    @EventListener
    public void on(BoardTaskMovedEvent event) { record(resolver.forBoard(event.boardId()), event.actorId(), "TASK_MOVED", "BOARD", event.boardId(), "Board task moved", null, event.toString()); }

    private void record(EventContextResolver.EventContext context, UUID actorId, String action, String entityType,
            UUID entityId, String message, String beforeValue, String afterValue) {
        activityService.record(context.workspaceId(), context.projectId(), actorId, action, entityType, entityId, message);
        auditLogService.record(context.workspaceId(), context.projectId(), actorId, action, entityType, entityId,
                beforeValue, afterValue, requestAuditContext.ipAddress(), requestAuditContext.userAgent());
    }
}
