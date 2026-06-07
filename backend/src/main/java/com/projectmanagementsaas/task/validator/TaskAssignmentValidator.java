package com.projectmanagementsaas.task.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.project.repository.ProjectMemberRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TaskAssignmentValidator {
    private final ProjectMemberRepository projectMemberRepository;

    public TaskAssignmentValidator(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    public void validateProjectMember(UUID projectId, UUID userId) {
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, userId)) {
            throw new BadRequestException("Assignee must be a project member");
        }
    }
}
