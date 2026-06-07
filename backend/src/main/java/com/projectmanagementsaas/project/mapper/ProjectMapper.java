package com.projectmanagementsaas.project.mapper;

import com.projectmanagementsaas.project.dto.ProjectMemberResponse;
import com.projectmanagementsaas.project.dto.ProjectResponse;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.entity.ProjectMember;
import com.projectmanagementsaas.project.entity.ProjectRole;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    public ProjectResponse toProjectResponse(Project project, ProjectRole currentUserRole) {
        return new ProjectResponse(
                project.getId(),
                project.getWorkspace().getId(),
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getStatus(),
                project.getColor(),
                project.getIcon(),
                project.getCreatedBy().getId(),
                currentUserRole,
                project.getCreatedAt(),
                project.getUpdatedAt());
    }

    public ProjectMemberResponse toMemberResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getDisplayName(),
                member.getRole(),
                member.getJoinedAt());
    }
}
