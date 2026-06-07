package com.projectmanagementsaas.ai.service;

import com.projectmanagementsaas.activity.repository.ActivityRepository;
import com.projectmanagementsaas.ai.dto.AiSearchResult;
import com.projectmanagementsaas.ai.entity.RagDocument;
import com.projectmanagementsaas.ai.entity.RagSourceType;
import com.projectmanagementsaas.ai.repository.RagDocumentRepository;
import com.projectmanagementsaas.document.repository.DocumentRepository;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.report.repository.ReportRepository;
import com.projectmanagementsaas.task.repository.TaskRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RagService {
    private final RagDocumentRepository ragRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final DocumentRepository documentRepository;
    private final ActivityRepository activityRepository;
    private final ReportRepository reportRepository;
    private final EmbeddingService embeddingService;

    public RagService(RagDocumentRepository ragRepository, ProjectRepository projectRepository,
            TaskRepository taskRepository, DocumentRepository documentRepository, ActivityRepository activityRepository,
            ReportRepository reportRepository, EmbeddingService embeddingService) {
        this.ragRepository = ragRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.documentRepository = documentRepository;
        this.activityRepository = activityRepository;
        this.reportRepository = reportRepository;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public void indexWorkspace(UUID workspaceId) {
        projectRepository.findByWorkspace_IdAndDeletedAtIsNullOrderByCreatedAtAsc(workspaceId).forEach(project -> {
            upsert(RagSourceType.PROJECT, project.getId(), workspaceId, project.getId(), project.getName(),
                    nullToEmpty(project.getDescription()));
            taskRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(project.getId()).forEach(task ->
                    upsert(RagSourceType.TASK, task.getId(), workspaceId, project.getId(), task.getTitle(),
                            task.getDescription() == null ? task.getTitle() : task.getTitle() + "\n" + task.getDescription()));
            documentRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(project.getId()).forEach(document ->
                    upsert(RagSourceType.DOCUMENT, document.getId(), workspaceId, project.getId(), document.getTitle(), document.getContent()));
        });
        activityRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).forEach(activity ->
                upsert(RagSourceType.ACTIVITY, activity.getId(), workspaceId, activity.getProjectId(),
                        activity.getAction(), activity.getMessage()));
        reportRepository.findAll().stream()
                .filter(report -> workspaceId.equals(report.getWorkspaceId()))
                .forEach(report -> upsert(RagSourceType.REPORT, report.getId(), workspaceId, report.getProjectId(),
                        report.getTitle(), report.getMetricsJson()));
    }

    @Transactional(readOnly = true)
    public List<AiSearchResult> search(UUID workspaceId, UUID projectId, String query) {
        List<Double> queryEmbedding = embeddingService.embed(query);
        List<RagDocument> documents = projectId == null
                ? ragRepository.findByWorkspaceId(workspaceId)
                : ragRepository.findByWorkspaceIdAndProjectId(workspaceId, projectId);
        return documents.stream()
                .map(document -> toResult(document, queryEmbedding))
                .sorted(Comparator.comparingDouble(AiSearchResult::score).reversed())
                .limit(10)
                .toList();
    }

    public String context(UUID workspaceId, UUID projectId, String query) {
        return search(workspaceId, projectId, query).stream()
                .map(result -> result.sourceType() + ": " + result.title() + " - " + result.snippet())
                .reduce("", (left, right) -> left + "\n" + right);
    }

    private AiSearchResult toResult(RagDocument document, List<Double> queryEmbedding) {
        double score = embeddingService.cosine(queryEmbedding, embeddingService.fromJson(document.getEmbeddingJson()));
        return new AiSearchResult(document.getSourceId(), document.getSourceType(), document.getTitle(), snippet(document.getContent()), score);
    }

    private void upsert(RagSourceType type, UUID sourceId, UUID workspaceId, UUID projectId, String title, String content) {
        RagDocument document = ragRepository.findBySourceTypeAndSourceId(type, sourceId).orElseGet(RagDocument::new);
        document.setSourceType(type);
        document.setSourceId(sourceId);
        document.setWorkspaceId(workspaceId);
        document.setProjectId(projectId);
        document.setTitle(title == null || title.isBlank() ? type.name() : title);
        document.setContent(nullToEmpty(content));
        document.setEmbeddingJson(embeddingService.toJson(embeddingService.embed(document.getTitle() + "\n" + document.getContent())));
        ragRepository.save(document);
    }

    private String snippet(String value) {
        String normalized = nullToEmpty(value).replaceAll("\\s+", " ").trim();
        return normalized.length() > 220 ? normalized.substring(0, 220) : normalized;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
