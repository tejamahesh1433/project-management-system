package com.projectmanagementsaas.project.service;

import com.projectmanagementsaas.common.exception.ForbiddenException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.entity.ProjectMember;
import com.projectmanagementsaas.project.entity.ProjectRole;
import com.projectmanagementsaas.project.repository.ProjectMemberRepository;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import com.projectmanagementsaas.workspace.repository.WorkspaceMemberRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProjectAccessService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public ProjectAccessService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            WorkspaceMemberRepository workspaceMemberRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public Project requireProject(UUID projectId) {
        return projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    public ProjectMember requireProjectMember(UUID projectId, UUID userId) {
        return projectMemberRepository.findByProject_IdAndUser_Id(projectId, userId)
                .filter(member -> member.getProject().getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    public ProjectMember requireProjectRole(UUID projectId, UUID userId, ProjectRole... allowedRoles) {
        ProjectMember member = requireProjectMember(projectId, userId);
        if (!Set.of(allowedRoles).contains(member.getRole())) {
            throw new ForbiddenException("Insufficient project permissions");
        }
        return member;
    }

    public void requireWorkspaceOwnerOrAdmin(UUID workspaceId, UUID userId) {
        workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)
                .filter(member -> member.getRole() == WorkspaceRole.OWNER || member.getRole() == WorkspaceRole.ADMIN)
                .orElseThrow(() -> new ForbiddenException("Only workspace OWNER or ADMIN can create projects"));
    }
}
