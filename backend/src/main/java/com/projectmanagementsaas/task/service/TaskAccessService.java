package com.projectmanagementsaas.task.service;

import com.projectmanagementsaas.common.exception.ForbiddenException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.project.entity.ProjectMember;
import com.projectmanagementsaas.project.entity.ProjectRole;
import com.projectmanagementsaas.project.service.ProjectAccessService;
import com.projectmanagementsaas.task.entity.Task;
import com.projectmanagementsaas.task.repository.TaskRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TaskAccessService {
    private final TaskRepository taskRepository;
    private final ProjectAccessService projectAccessService;

    public TaskAccessService(TaskRepository taskRepository, ProjectAccessService projectAccessService) {
        this.taskRepository = taskRepository;
        this.projectAccessService = projectAccessService;
    }

    public Task requireTask(UUID taskId) {
        return taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
    }

    public Task requireReadableTask(UUID taskId, UUID userId) {
        Task task = requireTask(taskId);
        projectAccessService.requireProjectMember(task.getProject().getId(), userId);
        return task;
    }

    public void requireReadableTaskForProject(UUID projectId, UUID userId) {
        projectAccessService.requireProjectMember(projectId, userId);
    }

    public ProjectMember requireContributor(UUID projectId, UUID userId) {
        ProjectMember member = projectAccessService.requireProjectMember(projectId, userId);
        if (member.getRole() == ProjectRole.PROJECT_VIEWER) {
            throw new ForbiddenException("PROJECT_VIEWER is read-only");
        }
        return member;
    }

    public ProjectMember requireManager(UUID projectId, UUID userId) {
        ProjectMember member = projectAccessService.requireProjectMember(projectId, userId);
        if (!Set.of(ProjectRole.PROJECT_OWNER, ProjectRole.PROJECT_ADMIN).contains(member.getRole())) {
            throw new ForbiddenException("Insufficient task permissions");
        }
        return member;
    }
}
