package com.projectmanagementsaas.activity.service;

import com.projectmanagementsaas.board.repository.BoardRepository;
import com.projectmanagementsaas.document.repository.DocumentRepository;
import com.projectmanagementsaas.file.repository.FileAssetRepository;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.sprint.repository.SprintRepository;
import com.projectmanagementsaas.task.repository.TaskRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EventContextResolver {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final DocumentRepository documentRepository;
    private final FileAssetRepository fileAssetRepository;
    private final BoardRepository boardRepository;

    public EventContextResolver(ProjectRepository projectRepository, TaskRepository taskRepository,
            SprintRepository sprintRepository, DocumentRepository documentRepository,
            FileAssetRepository fileAssetRepository, BoardRepository boardRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
        this.documentRepository = documentRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.boardRepository = boardRepository;
    }

    public EventContext forProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(project -> new EventContext(project.getWorkspace().getId(), project.getId()))
                .orElse(new EventContext(null, projectId));
    }

    public EventContext forTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .map(task -> forProject(task.getProject().getId()))
                .orElse(new EventContext(null, null));
    }

    public EventContext forSprint(UUID sprintId) {
        return sprintRepository.findById(sprintId)
                .map(sprint -> forProject(sprint.getProject().getId()))
                .orElse(new EventContext(null, null));
    }

    public EventContext forDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .map(document -> forProject(document.getProject().getId()))
                .orElse(new EventContext(null, null));
    }

    public EventContext forFile(UUID fileId) {
        return fileAssetRepository.findById(fileId)
                .map(file -> forProject(file.getProject().getId()))
                .orElse(new EventContext(null, null));
    }

    public EventContext forBoard(UUID boardId) {
        return boardRepository.findById(boardId)
                .map(board -> forProject(board.getProject().getId()))
                .orElse(new EventContext(null, null));
    }

    public Optional<UUID> projectIdForBoard(UUID boardId) {
        return boardRepository.findById(boardId).map(board -> board.getProject().getId());
    }

    public record EventContext(UUID workspaceId, UUID projectId) {
    }
}
