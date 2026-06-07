package com.projectmanagementsaas.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagementsaas.activity.repository.ActivityRepository;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.document.repository.DocumentRepository;
import com.projectmanagementsaas.events.model.ReportExportedEvent;
import com.projectmanagementsaas.events.model.ReportGeneratedEvent;
import com.projectmanagementsaas.file.repository.FileAssetRepository;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.repository.ProjectMemberRepository;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.project.service.ProjectAccessService;
import com.projectmanagementsaas.report.dto.GenerateReportRequest;
import com.projectmanagementsaas.report.dto.ReportResponse;
import com.projectmanagementsaas.report.entity.Report;
import com.projectmanagementsaas.report.entity.ReportSnapshot;
import com.projectmanagementsaas.report.mapper.ReportMapper;
import com.projectmanagementsaas.report.repository.ReportRepository;
import com.projectmanagementsaas.report.repository.ReportSnapshotRepository;
import com.projectmanagementsaas.report.validator.ReportRequestValidator;
import com.projectmanagementsaas.sprint.entity.Sprint;
import com.projectmanagementsaas.sprint.repository.SprintRepository;
import com.projectmanagementsaas.sprint.repository.SprintTaskRepository;
import com.projectmanagementsaas.task.entity.Task;
import com.projectmanagementsaas.task.entity.TaskStatus;
import com.projectmanagementsaas.task.repository.TaskRepository;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import com.projectmanagementsaas.workspace.entity.WorkspaceMember;
import com.projectmanagementsaas.workspace.repository.WorkspaceMemberRepository;
import com.projectmanagementsaas.workspace.service.WorkspaceAccessService;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportSnapshotRepository reportSnapshotRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final SprintTaskRepository sprintTaskRepository;
    private final DocumentRepository documentRepository;
    private final FileAssetRepository fileAssetRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService projectAccessService;
    private final WorkspaceAccessService workspaceAccessService;
    private final ReportRequestValidator reportRequestValidator;
    private final ReportMapper reportMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public ReportService(
            ReportRepository reportRepository,
            ReportSnapshotRepository reportSnapshotRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskRepository taskRepository,
            SprintRepository sprintRepository,
            SprintTaskRepository sprintTaskRepository,
            DocumentRepository documentRepository,
            FileAssetRepository fileAssetRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            ActivityRepository activityRepository,
            UserRepository userRepository,
            ProjectAccessService projectAccessService,
            WorkspaceAccessService workspaceAccessService,
            ReportRequestValidator reportRequestValidator,
            ReportMapper reportMapper,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.reportRepository = reportRepository;
        this.reportSnapshotRepository = reportSnapshotRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
        this.sprintTaskRepository = sprintTaskRepository;
        this.documentRepository = documentRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.projectAccessService = projectAccessService;
        this.workspaceAccessService = workspaceAccessService;
        this.reportRequestValidator = reportRequestValidator;
        this.reportMapper = reportMapper;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ReportResponse generate(UUID currentUserId, GenerateReportRequest request) {
        reportRequestValidator.validate(request);
        ReportData reportData = buildReport(currentUserId, request);
        User generatedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Report report = new Report();
        report.setType(request.type());
        report.setTitle(reportData.title());
        report.setWorkspaceId(reportData.workspaceId());
        report.setProjectId(reportData.projectId());
        report.setSprintId(reportData.sprintId());
        report.setGeneratedBy(generatedBy);
        report.setMetricsJson(toJson(reportData.metrics()));
        Report savedReport = reportRepository.save(report);

        ReportSnapshot snapshot = new ReportSnapshot();
        snapshot.setReport(savedReport);
        snapshot.setMetricsJson(savedReport.getMetricsJson());
        reportSnapshotRepository.save(snapshot);

        eventPublisher.publishEvent(new ReportGeneratedEvent(savedReport.getId(), savedReport.getType(), currentUserId));
        return reportMapper.toResponse(savedReport);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> list(UUID currentUserId) {
        return reportRepository.findByGeneratedBy_IdOrderByGeneratedAtDesc(currentUserId).stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportResponse get(UUID currentUserId, UUID reportId) {
        return reportMapper.toResponse(requireReport(currentUserId, reportId));
    }

    @Transactional
    public String exportJson(UUID currentUserId, UUID reportId) {
        Report report = requireReport(currentUserId, reportId);
        String json = latestSnapshot(report).getMetricsJson();
        eventPublisher.publishEvent(new ReportExportedEvent(report.getId(), report.getType(), "JSON", currentUserId));
        return json;
    }

    @Transactional
    public String exportCsv(UUID currentUserId, UUID reportId) {
        Report report = requireReport(currentUserId, reportId);
        String csv = toCsv(latestSnapshot(report).getMetricsJson());
        eventPublisher.publishEvent(new ReportExportedEvent(report.getId(), report.getType(), "CSV", currentUserId));
        return csv;
    }

    private ReportData buildReport(UUID currentUserId, GenerateReportRequest request) {
        return switch (request.type()) {
            case PROJECT -> projectReport(currentUserId, request.projectId());
            case SPRINT -> sprintReport(currentUserId, request.sprintId());
            case TEAM -> teamReport(currentUserId, request.projectId());
            case WORKSPACE -> workspaceReport(currentUserId, request.workspaceId());
        };
    }

    private ReportData projectReport(UUID currentUserId, UUID projectId) {
        Project project = projectAccessService.requireProject(projectId);
        projectAccessService.requireProjectMember(projectId, currentUserId);
        List<Task> tasks = taskRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId);
        long completed = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        Map<String, Object> metrics = orderedMetrics();
        metrics.put("totalTasks", tasks.size());
        metrics.put("completedTasks", completed);
        metrics.put("openTasks", tasks.size() - completed);
        metrics.put("members", projectMemberRepository.findByProject_Id(projectId).size());
        metrics.put("completionPercentage", percentage(completed, tasks.size()));
        return new ReportData(project.getName() + " Project Report", project.getWorkspace().getId(), projectId, null, metrics);
    }

    private ReportData sprintReport(UUID currentUserId, UUID sprintId) {
        Sprint sprint = sprintRepository.findByIdAndDeletedAtIsNull(sprintId)
                .orElseThrow(() -> new NotFoundException("Sprint not found"));
        projectAccessService.requireProjectMember(sprint.getProject().getId(), currentUserId);
        List<Task> tasks = sprintTaskRepository.findBySprint_IdOrderByAddedAtAsc(sprintId).stream()
                .map(com.projectmanagementsaas.sprint.entity.SprintTask::getTask)
                .toList();
        long completed = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        int storyPointsCompleted = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .mapToInt(Task::getStoryPoints)
                .sum();
        int storyPointsTotal = tasks.stream().mapToInt(Task::getStoryPoints).sum();
        Map<String, Object> metrics = orderedMetrics();
        metrics.put("totalTasks", tasks.size());
        metrics.put("completedTasks", completed);
        metrics.put("remainingTasks", tasks.size() - completed);
        metrics.put("storyPointsCompleted", storyPointsCompleted);
        metrics.put("storyPointsRemaining", storyPointsTotal - storyPointsCompleted);
        return new ReportData(sprint.getName() + " Sprint Report", sprint.getProject().getWorkspace().getId(),
                sprint.getProject().getId(), sprintId, metrics);
    }

    private ReportData teamReport(UUID currentUserId, UUID projectId) {
        Project project = projectAccessService.requireProject(projectId);
        projectAccessService.requireProjectMember(projectId, currentUserId);
        List<Task> tasks = taskRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).stream()
                .filter(task -> task.getAssignee() != null)
                .toList();
        List<Task> completedTasks = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).toList();
        long completed = completedTasks.size();
        double averageCompletionTimeHours = completedTasks.isEmpty() ? 0.0 : completedTasks.stream()
                .mapToLong(task -> Duration.between(task.getCreatedAt(), task.getUpdatedAt()).toHours())
                .average()
                .orElse(0.0);

        Map<String, Object> metrics = orderedMetrics();
        metrics.put("assignedTasks", tasks.size());
        metrics.put("completedTasks", completed);
        metrics.put("openTasks", tasks.size() - completed);
        metrics.put("averageCompletionTimeHours", averageCompletionTimeHours);
        return new ReportData(project.getName() + " Team Report", project.getWorkspace().getId(), projectId, null, metrics);
    }

    private ReportData workspaceReport(UUID currentUserId, UUID workspaceId) {
        WorkspaceMember member = workspaceAccessService.requireMembership(workspaceId, currentUserId);
        List<Project> projects = projectRepository.findByWorkspace_IdAndDeletedAtIsNullOrderByCreatedAtAsc(workspaceId);
        List<UUID> projectIds = projects.stream().map(Project::getId).toList();
        int taskCount = projectIds.stream()
                .mapToInt(projectId -> taskRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).size())
                .sum();
        int documentCount = projectIds.stream()
                .mapToInt(projectId -> documentRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).size())
                .sum();
        int fileCount = projectIds.stream()
                .mapToInt(projectId -> fileAssetRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).size())
                .sum();

        Map<String, Object> metrics = orderedMetrics();
        metrics.put("projects", projects.size());
        metrics.put("tasks", taskCount);
        metrics.put("documents", documentCount);
        metrics.put("files", fileCount);
        metrics.put("members", workspaceMemberRepository.findByWorkspace_Id(workspaceId).size());
        metrics.put("activityCount", activityRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).size());
        return new ReportData(member.getWorkspace().getName() + " Workspace Report", workspaceId, null, null, metrics);
    }

    private Report requireReport(UUID currentUserId, UUID reportId) {
        return reportRepository.findByIdAndGeneratedBy_Id(reportId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Report not found"));
    }

    private ReportSnapshot latestSnapshot(Report report) {
        return reportSnapshotRepository.findFirstByReport_IdOrderByCreatedAtDesc(report.getId())
                .orElseThrow(() -> new NotFoundException("Report snapshot not found"));
    }

    private Map<String, Object> orderedMetrics() {
        return new LinkedHashMap<>();
    }

    private double percentage(long completed, int total) {
        return total == 0 ? 0.0 : (completed * 100.0) / total;
    }

    private String toJson(Map<String, Object> metrics) {
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize report metrics", exception);
        }
    }

    private String toCsv(String metricsJson) {
        try {
            JsonNode root = objectMapper.readTree(metricsJson);
            StringBuilder header = new StringBuilder();
            StringBuilder values = new StringBuilder();
            root.fields().forEachRemaining(entry -> {
                if (!header.isEmpty()) {
                    header.append(',');
                    values.append(',');
                }
                header.append(csv(entry.getKey()));
                values.append(csv(entry.getValue().isNumber() ? entry.getValue().asText() : entry.getValue().asText()));
            });
            return header.append('\n').append(values).append('\n').toString();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to export report metrics", exception);
        }
    }

    private String csv(String value) {
        String escaped = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private record ReportData(String title, UUID workspaceId, UUID projectId, UUID sprintId, Map<String, Object> metrics) {
    }
}
