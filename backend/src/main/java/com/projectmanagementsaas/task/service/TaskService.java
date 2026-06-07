package com.projectmanagementsaas.task.service;

import com.projectmanagementsaas.common.exception.ForbiddenException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.events.model.TaskAssignedEvent;
import com.projectmanagementsaas.events.model.TaskCommentCreatedEvent;
import com.projectmanagementsaas.events.model.TaskCreatedEvent;
import com.projectmanagementsaas.events.model.TaskDeletedEvent;
import com.projectmanagementsaas.events.model.TaskLabelAddedEvent;
import com.projectmanagementsaas.events.model.TaskStatusChangedEvent;
import com.projectmanagementsaas.events.model.TaskUpdatedEvent;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.task.dto.AddTaskLabelRequest;
import com.projectmanagementsaas.task.dto.AssignTaskRequest;
import com.projectmanagementsaas.task.dto.ChangeTaskStatusRequest;
import com.projectmanagementsaas.task.dto.CreateLabelRequest;
import com.projectmanagementsaas.task.dto.CreateTaskCommentRequest;
import com.projectmanagementsaas.task.dto.CreateTaskRequest;
import com.projectmanagementsaas.task.dto.LabelResponse;
import com.projectmanagementsaas.task.dto.TaskCommentResponse;
import com.projectmanagementsaas.task.dto.TaskResponse;
import com.projectmanagementsaas.task.dto.UpdateTaskCommentRequest;
import com.projectmanagementsaas.task.dto.UpdateTaskRequest;
import com.projectmanagementsaas.task.entity.Label;
import com.projectmanagementsaas.task.entity.Task;
import com.projectmanagementsaas.task.entity.TaskComment;
import com.projectmanagementsaas.task.entity.TaskLabel;
import com.projectmanagementsaas.task.entity.TaskPriority;
import com.projectmanagementsaas.task.entity.TaskType;
import com.projectmanagementsaas.task.mapper.TaskMapper;
import com.projectmanagementsaas.task.repository.LabelRepository;
import com.projectmanagementsaas.task.repository.TaskCommentRepository;
import com.projectmanagementsaas.task.repository.TaskLabelRepository;
import com.projectmanagementsaas.task.repository.TaskRepository;
import com.projectmanagementsaas.task.validator.LabelValidator;
import com.projectmanagementsaas.task.validator.TaskAssignmentValidator;
import com.projectmanagementsaas.task.validator.TaskHierarchyValidator;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final LabelRepository labelRepository;
    private final TaskLabelRepository taskLabelRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskAccessService taskAccessService;
    private final TaskMapper taskMapper;
    private final TaskHierarchyValidator taskHierarchyValidator;
    private final TaskAssignmentValidator taskAssignmentValidator;
    private final LabelValidator labelValidator;
    private final ApplicationEventPublisher eventPublisher;

    public TaskService(
            TaskRepository taskRepository,
            TaskCommentRepository taskCommentRepository,
            LabelRepository labelRepository,
            TaskLabelRepository taskLabelRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TaskAccessService taskAccessService,
            TaskMapper taskMapper,
            TaskHierarchyValidator taskHierarchyValidator,
            TaskAssignmentValidator taskAssignmentValidator,
            LabelValidator labelValidator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.labelRepository = labelRepository;
        this.taskLabelRepository = taskLabelRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskAccessService = taskAccessService;
        this.taskMapper = taskMapper;
        this.taskHierarchyValidator = taskHierarchyValidator;
        this.taskAssignmentValidator = taskAssignmentValidator;
        this.labelValidator = labelValidator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TaskResponse createTask(UUID currentUserId, CreateTaskRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));
        taskAccessService.requireContributor(project.getId(), currentUserId);
        Task parentTask = resolveParentTask(request.parentTaskId());
        taskHierarchyValidator.validateParent(parentTask, project.getId(), null);
        User assignee = resolveAssignee(project.getId(), request.assigneeId());

        Task task = new Task();
        task.setProject(project);
        task.setParentTask(parentTask);
        task.setTitle(request.title().trim());
        task.setDescription(normalizeOptional(request.description()));
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        task.setType(request.type() == null ? TaskType.TASK : request.type());
        task.setAssignee(assignee);
        task.setCreatedBy(getUser(currentUserId));
        task.setDueDate(request.dueDate());
        task.setStoryPoints(request.storyPoints() == null ? 0 : request.storyPoints());
        Task savedTask = taskRepository.save(task);
        eventPublisher.publishEvent(new TaskCreatedEvent(savedTask.getId(), project.getId(), currentUserId));
        return taskMapper.toTaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(UUID currentUserId, UUID projectId) {
        taskAccessService.requireReadableTaskForProject(projectId, currentUserId);
        return taskRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).stream()
                .map(taskMapper::toTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID currentUserId, UUID taskId) {
        return taskMapper.toTaskResponse(taskAccessService.requireReadableTask(taskId, currentUserId));
    }

    @Transactional
    public TaskResponse updateTask(UUID currentUserId, UUID taskId, UpdateTaskRequest request) {
        Task task = taskAccessService.requireReadableTask(taskId, currentUserId);
        taskAccessService.requireContributor(task.getProject().getId(), currentUserId);
        Task parentTask = resolveParentTask(request.parentTaskId());
        taskHierarchyValidator.validateParent(parentTask, task.getProject().getId(), task.getId());
        User assignee = resolveAssignee(task.getProject().getId(), request.assigneeId());

        task.setParentTask(parentTask);
        task.setTitle(request.title().trim());
        task.setDescription(normalizeOptional(request.description()));
        task.setStatus(request.status() == null ? task.getStatus() : request.status());
        task.setPriority(request.priority() == null ? task.getPriority() : request.priority());
        task.setType(request.type() == null ? task.getType() : request.type());
        task.setAssignee(assignee);
        task.setDueDate(request.dueDate());
        task.setStoryPoints(request.storyPoints() == null ? task.getStoryPoints() : request.storyPoints());
        task.setUpdatedAt(Instant.now());
        Task savedTask = taskRepository.save(task);
        eventPublisher.publishEvent(new TaskUpdatedEvent(taskId, task.getProject().getId(), currentUserId));
        return taskMapper.toTaskResponse(savedTask);
    }

    @Transactional
    public void deleteTask(UUID currentUserId, UUID taskId) {
        Task task = taskAccessService.requireReadableTask(taskId, currentUserId);
        taskAccessService.requireContributor(task.getProject().getId(), currentUserId);
        task.softDelete();
        taskRepository.save(task);
        eventPublisher.publishEvent(new TaskDeletedEvent(taskId, task.getProject().getId(), currentUserId));
    }

    @Transactional
    public TaskResponse assignTask(UUID currentUserId, UUID taskId, AssignTaskRequest request) {
        Task task = taskAccessService.requireReadableTask(taskId, currentUserId);
        taskAccessService.requireContributor(task.getProject().getId(), currentUserId);
        User assignee = resolveAssignee(task.getProject().getId(), request.assigneeId());
        task.setAssignee(assignee);
        task.setUpdatedAt(Instant.now());
        Task savedTask = taskRepository.save(task);
        eventPublisher.publishEvent(new TaskAssignedEvent(taskId, assignee.getId(), currentUserId));
        return taskMapper.toTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse changeStatus(UUID currentUserId, UUID taskId, ChangeTaskStatusRequest request) {
        Task task = taskAccessService.requireReadableTask(taskId, currentUserId);
        taskAccessService.requireContributor(task.getProject().getId(), currentUserId);
        task.setStatus(request.status());
        task.setUpdatedAt(Instant.now());
        Task savedTask = taskRepository.save(task);
        eventPublisher.publishEvent(new TaskStatusChangedEvent(taskId, request.status(), currentUserId));
        return taskMapper.toTaskResponse(savedTask);
    }

    @Transactional
    public TaskCommentResponse createComment(UUID currentUserId, UUID taskId, CreateTaskCommentRequest request) {
        Task task = taskAccessService.requireReadableTask(taskId, currentUserId);
        taskAccessService.requireContributor(task.getProject().getId(), currentUserId);
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthor(getUser(currentUserId));
        comment.setBody(request.body().trim());
        TaskComment savedComment = taskCommentRepository.save(comment);
        eventPublisher.publishEvent(new TaskCommentCreatedEvent(taskId, savedComment.getId(), currentUserId));
        return taskMapper.toCommentResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponse> listComments(UUID currentUserId, UUID taskId) {
        taskAccessService.requireReadableTask(taskId, currentUserId);
        return taskCommentRepository.findByTask_IdAndDeletedAtIsNullOrderByCreatedAtAsc(taskId).stream()
                .map(taskMapper::toCommentResponse)
                .toList();
    }

    @Transactional
    public TaskCommentResponse updateComment(UUID currentUserId, UUID taskId, UUID commentId, UpdateTaskCommentRequest request) {
        TaskComment comment = getComment(taskId, commentId);
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("Only the comment author can update this comment");
        }
        comment.setBody(request.body().trim());
        comment.setUpdatedAt(Instant.now());
        return taskMapper.toCommentResponse(taskCommentRepository.save(comment));
    }

    @Transactional
    public LabelResponse createLabel(UUID currentUserId, CreateLabelRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));
        taskAccessService.requireContributor(project.getId(), currentUserId);
        String name = request.name().trim();
        labelValidator.validateUnique(project.getId(), name);
        Label label = new Label();
        label.setProject(project);
        label.setName(name);
        label.setColor(request.color().trim());
        return taskMapper.toLabelResponse(labelRepository.save(label));
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> listLabels(UUID currentUserId, UUID projectId) {
        taskAccessService.requireReadableTaskForProject(projectId, currentUserId);
        return labelRepository.findByProject_IdOrderByNameAsc(projectId).stream()
                .map(taskMapper::toLabelResponse)
                .toList();
    }

    @Transactional
    public TaskResponse addLabel(UUID currentUserId, UUID taskId, AddTaskLabelRequest request) {
        Task task = taskAccessService.requireReadableTask(taskId, currentUserId);
        taskAccessService.requireContributor(task.getProject().getId(), currentUserId);
        Label label = labelRepository.findByIdAndProject_Id(request.labelId(), task.getProject().getId())
                .orElseThrow(() -> new NotFoundException("Label not found"));
        if (!taskLabelRepository.existsByTask_IdAndLabel_Id(taskId, label.getId())) {
            TaskLabel taskLabel = new TaskLabel();
            taskLabel.setTask(task);
            taskLabel.setLabel(label);
            taskLabelRepository.save(taskLabel);
            eventPublisher.publishEvent(new TaskLabelAddedEvent(taskId, label.getId(), currentUserId));
        }
        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    public TaskResponse removeLabel(UUID currentUserId, UUID taskId, UUID labelId) {
        Task task = taskAccessService.requireReadableTask(taskId, currentUserId);
        taskAccessService.requireContributor(task.getProject().getId(), currentUserId);
        taskLabelRepository.findByTask_IdAndLabel_Id(taskId, labelId).ifPresent(taskLabelRepository::delete);
        return taskMapper.toTaskResponse(task);
    }

    private Task resolveParentTask(UUID parentTaskId) {
        if (parentTaskId == null) {
            return null;
        }
        return taskRepository.findByIdAndDeletedAtIsNull(parentTaskId)
                .orElseThrow(() -> new NotFoundException("Parent task not found"));
    }

    private User resolveAssignee(UUID projectId, UUID assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        taskAssignmentValidator.validateProjectMember(projectId, assigneeId);
        return getUser(assigneeId);
    }

    private TaskComment getComment(UUID taskId, UUID commentId) {
        return taskCommentRepository.findByIdAndDeletedAtIsNull(commentId)
                .filter(comment -> comment.getTask().getId().equals(taskId))
                .orElseThrow(() -> new NotFoundException("Task comment not found"));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
