package com.projectmanagementsaas.sprint.service;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.events.model.SprintCancelledEvent;
import com.projectmanagementsaas.events.model.SprintCompletedEvent;
import com.projectmanagementsaas.events.model.SprintCreatedEvent;
import com.projectmanagementsaas.events.model.SprintStartedEvent;
import com.projectmanagementsaas.events.model.SprintTaskAddedEvent;
import com.projectmanagementsaas.events.model.SprintTaskRemovedEvent;
import com.projectmanagementsaas.events.model.SprintUpdatedEvent;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.sprint.dto.AddSprintTaskRequest;
import com.projectmanagementsaas.sprint.dto.CreateSprintRequest;
import com.projectmanagementsaas.sprint.dto.SprintMetricsResponse;
import com.projectmanagementsaas.sprint.dto.SprintResponse;
import com.projectmanagementsaas.sprint.dto.SprintTaskResponse;
import com.projectmanagementsaas.sprint.dto.UpdateSprintRequest;
import com.projectmanagementsaas.sprint.entity.Sprint;
import com.projectmanagementsaas.sprint.entity.SprintStatus;
import com.projectmanagementsaas.sprint.entity.SprintTask;
import com.projectmanagementsaas.sprint.mapper.SprintMapper;
import com.projectmanagementsaas.sprint.repository.SprintRepository;
import com.projectmanagementsaas.sprint.repository.SprintTaskRepository;
import com.projectmanagementsaas.sprint.validator.ActiveSprintValidator;
import com.projectmanagementsaas.sprint.validator.SprintDateValidator;
import com.projectmanagementsaas.sprint.validator.SprintTaskValidator;
import com.projectmanagementsaas.task.entity.Task;
import com.projectmanagementsaas.task.entity.TaskStatus;
import com.projectmanagementsaas.task.service.TaskAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SprintService {
    private final SprintRepository sprintRepository;
    private final SprintTaskRepository sprintTaskRepository;
    private final ProjectRepository projectRepository;
    private final TaskAccessService taskAccessService;
    private final SprintMapper sprintMapper;
    private final SprintDateValidator sprintDateValidator;
    private final ActiveSprintValidator activeSprintValidator;
    private final SprintTaskValidator sprintTaskValidator;
    private final ApplicationEventPublisher eventPublisher;

    public SprintService(
            SprintRepository sprintRepository,
            SprintTaskRepository sprintTaskRepository,
            ProjectRepository projectRepository,
            TaskAccessService taskAccessService,
            SprintMapper sprintMapper,
            SprintDateValidator sprintDateValidator,
            ActiveSprintValidator activeSprintValidator,
            SprintTaskValidator sprintTaskValidator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.sprintRepository = sprintRepository;
        this.sprintTaskRepository = sprintTaskRepository;
        this.projectRepository = projectRepository;
        this.taskAccessService = taskAccessService;
        this.sprintMapper = sprintMapper;
        this.sprintDateValidator = sprintDateValidator;
        this.activeSprintValidator = activeSprintValidator;
        this.sprintTaskValidator = sprintTaskValidator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SprintResponse createSprint(UUID currentUserId, CreateSprintRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));
        taskAccessService.requireManager(project.getId(), currentUserId);
        sprintDateValidator.validate(request.startDate(), request.endDate());

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(request.name().trim());
        sprint.setGoal(normalizeOptional(request.goal()));
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        Sprint savedSprint = sprintRepository.save(sprint);
        eventPublisher.publishEvent(new SprintCreatedEvent(savedSprint.getId(), project.getId(), currentUserId));
        return sprintMapper.toSprintResponse(savedSprint);
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> listSprints(UUID currentUserId, UUID projectId) {
        taskAccessService.requireReadableTaskForProject(projectId, currentUserId);
        return sprintRepository.findByProject_IdAndDeletedAtIsNullOrderByStartDateAsc(projectId).stream()
                .map(sprintMapper::toSprintResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SprintResponse getSprint(UUID currentUserId, UUID sprintId) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireReadableTaskForProject(sprint.getProject().getId(), currentUserId);
        return sprintMapper.toSprintResponse(sprint);
    }

    @Transactional
    public SprintResponse updateSprint(UUID currentUserId, UUID sprintId, UpdateSprintRequest request) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireManager(sprint.getProject().getId(), currentUserId);
        if (sprint.getStatus() == SprintStatus.COMPLETED || sprint.getStatus() == SprintStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a completed or cancelled sprint");
        }
        sprintDateValidator.validate(request.startDate(), request.endDate());
        sprint.setName(request.name().trim());
        sprint.setGoal(normalizeOptional(request.goal()));
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setUpdatedAt(Instant.now());
        Sprint savedSprint = sprintRepository.save(sprint);
        eventPublisher.publishEvent(new SprintUpdatedEvent(sprintId, sprint.getProject().getId(), currentUserId));
        return sprintMapper.toSprintResponse(savedSprint);
    }

    @Transactional
    public void deleteSprint(UUID currentUserId, UUID sprintId) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireManager(sprint.getProject().getId(), currentUserId);
        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            throw new BadRequestException("Cannot delete an active sprint");
        }
        sprint.softDelete();
        sprintRepository.save(sprint);
    }

    @Transactional
    public SprintResponse startSprint(UUID currentUserId, UUID sprintId) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireManager(sprint.getProject().getId(), currentUserId);
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new BadRequestException("Only planned sprints can be started");
        }
        activeSprintValidator.validateNoActiveSprint(sprint.getProject().getId());
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setUpdatedAt(Instant.now());
        Sprint savedSprint = sprintRepository.save(sprint);
        eventPublisher.publishEvent(new SprintStartedEvent(sprintId, sprint.getProject().getId(), currentUserId));
        return sprintMapper.toSprintResponse(savedSprint);
    }

    @Transactional
    public SprintResponse completeSprint(UUID currentUserId, UUID sprintId) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireManager(sprint.getProject().getId(), currentUserId);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BadRequestException("Only active sprints can be completed");
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setUpdatedAt(Instant.now());
        Sprint savedSprint = sprintRepository.save(sprint);
        eventPublisher.publishEvent(new SprintCompletedEvent(sprintId, sprint.getProject().getId(), currentUserId));
        return sprintMapper.toSprintResponse(savedSprint);
    }

    @Transactional
    public SprintResponse cancelSprint(UUID currentUserId, UUID sprintId) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireManager(sprint.getProject().getId(), currentUserId);
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed sprint");
        }
        sprint.setStatus(SprintStatus.CANCELLED);
        sprint.setUpdatedAt(Instant.now());
        Sprint savedSprint = sprintRepository.save(sprint);
        eventPublisher.publishEvent(new SprintCancelledEvent(sprintId, sprint.getProject().getId(), currentUserId));
        return sprintMapper.toSprintResponse(savedSprint);
    }

    @Transactional
    public SprintTaskResponse addTask(UUID currentUserId, UUID sprintId, AddSprintTaskRequest request) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireContributor(sprint.getProject().getId(), currentUserId);
        Task task = taskAccessService.requireTask(request.taskId());
        sprintTaskValidator.validateTaskBelongsToSprintProject(sprint, task);
        if (sprintTaskRepository.existsBySprint_IdAndTask_Id(sprintId, task.getId())) {
            throw new BadRequestException("Task is already assigned to this sprint");
        }

        SprintTask sprintTask = new SprintTask();
        sprintTask.setSprint(sprint);
        sprintTask.setTask(task);
        SprintTask savedSprintTask = sprintTaskRepository.save(sprintTask);
        eventPublisher.publishEvent(new SprintTaskAddedEvent(sprintId, task.getId(), currentUserId));
        return sprintMapper.toSprintTaskResponse(savedSprintTask);
    }

    @Transactional
    public void removeTask(UUID currentUserId, UUID sprintId, UUID taskId) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireContributor(sprint.getProject().getId(), currentUserId);
        SprintTask sprintTask = sprintTaskRepository.findBySprint_IdAndTask_Id(sprintId, taskId)
                .orElseThrow(() -> new NotFoundException("Sprint task not found"));
        sprintTaskRepository.delete(sprintTask);
        eventPublisher.publishEvent(new SprintTaskRemovedEvent(sprintId, taskId, currentUserId));
    }

    @Transactional(readOnly = true)
    public List<SprintTaskResponse> listTasks(UUID currentUserId, UUID sprintId) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireReadableTaskForProject(sprint.getProject().getId(), currentUserId);
        return sprintTaskRepository.findBySprint_IdOrderByAddedAtAsc(sprintId).stream()
                .map(sprintMapper::toSprintTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SprintMetricsResponse metrics(UUID currentUserId, UUID sprintId) {
        Sprint sprint = requireSprint(sprintId);
        taskAccessService.requireReadableTaskForProject(sprint.getProject().getId(), currentUserId);
        List<SprintTask> sprintTasks = sprintTaskRepository.findBySprint_IdOrderByAddedAtAsc(sprintId);
        int totalTasks = sprintTasks.size();
        int completedTasks = (int) sprintTasks.stream()
                .filter(sprintTask -> sprintTask.getTask().getStatus() == TaskStatus.DONE)
                .count();
        int storyPointsCompleted = sprintTasks.stream()
                .filter(sprintTask -> sprintTask.getTask().getStatus() == TaskStatus.DONE)
                .mapToInt(sprintTask -> sprintTask.getTask().getStoryPoints())
                .sum();
        int storyPointsTotal = sprintTasks.stream()
                .mapToInt(sprintTask -> sprintTask.getTask().getStoryPoints())
                .sum();
        double completionPercentage = totalTasks == 0 ? 0.0 : (completedTasks * 100.0) / totalTasks;

        return new SprintMetricsResponse(
                sprintId,
                totalTasks,
                completedTasks,
                totalTasks - completedTasks,
                completionPercentage,
                storyPointsCompleted,
                storyPointsTotal - storyPointsCompleted);
    }

    private Sprint requireSprint(UUID sprintId) {
        return sprintRepository.findByIdAndDeletedAtIsNull(sprintId)
                .orElseThrow(() -> new NotFoundException("Sprint not found"));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
