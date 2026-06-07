package com.projectmanagementsaas.analytics.service;

import com.projectmanagementsaas.activity.repository.ActivityRepository;
import com.projectmanagementsaas.analytics.dto.ProjectAnalyticsResponse;
import com.projectmanagementsaas.analytics.dto.SprintAnalyticsResponse;
import com.projectmanagementsaas.analytics.dto.TeamAnalyticsResponse;
import com.projectmanagementsaas.analytics.dto.WorkspaceAnalyticsResponse;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.document.repository.DocumentRepository;
import com.projectmanagementsaas.events.model.AnalyticsViewedEvent;
import com.projectmanagementsaas.file.repository.FileAssetRepository;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.project.service.ProjectAccessService;
import com.projectmanagementsaas.sprint.entity.Sprint;
import com.projectmanagementsaas.sprint.repository.SprintRepository;
import com.projectmanagementsaas.sprint.repository.SprintTaskRepository;
import com.projectmanagementsaas.task.entity.Task;
import com.projectmanagementsaas.task.entity.TaskStatus;
import com.projectmanagementsaas.task.entity.TaskType;
import com.projectmanagementsaas.task.repository.TaskRepository;
import com.projectmanagementsaas.workspace.repository.WorkspaceMemberRepository;
import com.projectmanagementsaas.workspace.service.WorkspaceAccessService;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final SprintTaskRepository sprintTaskRepository;
    private final DocumentRepository documentRepository;
    private final FileAssetRepository fileAssetRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ActivityRepository activityRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final ProjectAccessService projectAccessService;
    private final ApplicationEventPublisher eventPublisher;

    public AnalyticsService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            SprintRepository sprintRepository,
            SprintTaskRepository sprintTaskRepository,
            DocumentRepository documentRepository,
            FileAssetRepository fileAssetRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            ActivityRepository activityRepository,
            WorkspaceAccessService workspaceAccessService,
            ProjectAccessService projectAccessService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
        this.sprintTaskRepository = sprintTaskRepository;
        this.documentRepository = documentRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.activityRepository = activityRepository;
        this.workspaceAccessService = workspaceAccessService;
        this.projectAccessService = projectAccessService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public WorkspaceAnalyticsResponse workspace(UUID currentUserId, UUID workspaceId) {
        workspaceAccessService.requireMembership(workspaceId, currentUserId);
        List<Project> projects = projectRepository.findByWorkspace_IdAndDeletedAtIsNullOrderByCreatedAtAsc(workspaceId);
        List<UUID> projectIds = projects.stream().map(Project::getId).toList();
        WorkspaceAnalyticsResponse response = new WorkspaceAnalyticsResponse(
                workspaceId,
                projects.size(),
                sum(projectIds, projectId -> taskRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).size()),
                sum(projectIds, projectId -> documentRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).size()),
                sum(projectIds, projectId -> fileAssetRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).size()),
                workspaceMemberRepository.findByWorkspace_Id(workspaceId).size(),
                activityRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).size());
        eventPublisher.publishEvent(new AnalyticsViewedEvent("WORKSPACE", workspaceId, currentUserId));
        return response;
    }

    @Transactional
    public ProjectAnalyticsResponse project(UUID currentUserId, UUID projectId) {
        Project project = projectAccessService.requireProject(projectId);
        projectAccessService.requireProjectMember(projectId, currentUserId);
        List<Task> tasks = taskRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId);
        Map<String, Long> taskDistribution = enumBreakdown(tasks, TaskType.values(), task -> task.getType().name());
        Map<String, Long> statusBreakdown = enumBreakdown(tasks, TaskStatus.values(), task -> task.getStatus().name());
        double sprintProgress = sprintRepository.findByProject_IdAndDeletedAtIsNullOrderByStartDateAsc(project.getId()).stream()
                .mapToDouble(sprint -> sprint(sprint.getId()).completionPercentage())
                .average()
                .orElse(0.0);
        eventPublisher.publishEvent(new AnalyticsViewedEvent("PROJECT", projectId, currentUserId));
        return new ProjectAnalyticsResponse(projectId, tasks.size(), taskDistribution, statusBreakdown, sprintProgress);
    }

    @Transactional
    public SprintAnalyticsResponse sprint(UUID currentUserId, UUID sprintId) {
        Sprint sprint = sprintRepository.findByIdAndDeletedAtIsNull(sprintId)
                .orElseThrow(() -> new NotFoundException("Sprint not found"));
        projectAccessService.requireProjectMember(sprint.getProject().getId(), currentUserId);
        SprintAnalyticsResponse response = sprint(sprintId);
        eventPublisher.publishEvent(new AnalyticsViewedEvent("SPRINT", sprintId, currentUserId));
        return response;
    }

    @Transactional
    public TeamAnalyticsResponse team(UUID currentUserId, UUID projectId) {
        projectAccessService.requireProjectMember(projectId, currentUserId);
        List<Task> assignedTasks = taskRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).stream()
                .filter(task -> task.getAssignee() != null)
                .toList();
        long completed = assignedTasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        double averageCompletionTimeHours = assignedTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .mapToLong(task -> Duration.between(task.getCreatedAt(), task.getUpdatedAt()).toHours())
                .average()
                .orElse(0.0);
        long overdue = assignedTasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> task.getDueDate().isBefore(LocalDate.now()))
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .count();
        eventPublisher.publishEvent(new AnalyticsViewedEvent("TEAM", projectId, currentUserId));
        return new TeamAnalyticsResponse(projectId, assignedTasks.size(), (int) completed,
                (int) (assignedTasks.size() - completed), averageCompletionTimeHours, (int) overdue);
    }

    private SprintAnalyticsResponse sprint(UUID sprintId) {
        List<Task> tasks = sprintTaskRepository.findBySprint_IdOrderByAddedAtAsc(sprintId).stream()
                .map(com.projectmanagementsaas.sprint.entity.SprintTask::getTask)
                .toList();
        long completed = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        int storyPointsCompleted = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .mapToInt(Task::getStoryPoints)
                .sum();
        int storyPointsTotal = tasks.stream().mapToInt(Task::getStoryPoints).sum();
        return new SprintAnalyticsResponse(sprintId, storyPointsCompleted, percentage(completed, tasks.size()),
                storyPointsCompleted, storyPointsTotal - storyPointsCompleted);
    }

    private int sum(List<UUID> ids, Function<UUID, Integer> counter) {
        return ids.stream().mapToInt(counter::apply).sum();
    }

    private <E extends Enum<E>> Map<String, Long> enumBreakdown(List<Task> tasks, E[] values, Function<Task, String> classifier) {
        Map<String, Long> counts = tasks.stream().collect(Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> ordered = new LinkedHashMap<>();
        Arrays.stream(values).forEach(value -> ordered.put(value.name(), counts.getOrDefault(value.name(), 0L)));
        return ordered;
    }

    private double percentage(long completed, int total) {
        return total == 0 ? 0.0 : (completed * 100.0) / total;
    }
}
