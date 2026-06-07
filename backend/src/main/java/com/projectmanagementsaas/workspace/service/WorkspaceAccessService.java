package com.projectmanagementsaas.workspace.service;

import com.projectmanagementsaas.common.exception.ForbiddenException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.workspace.entity.WorkspaceMember;
import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import com.projectmanagementsaas.workspace.repository.WorkspaceMemberRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceAccessService {
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspaceAccessService(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public WorkspaceMember requireMembership(UUID workspaceId, UUID userId) {
        return workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
    }

    public WorkspaceMember requireRole(UUID workspaceId, UUID userId, WorkspaceRole... allowedRoles) {
        WorkspaceMember member = requireMembership(workspaceId, userId);
        if (!Set.of(allowedRoles).contains(member.getRole())) {
            throw new ForbiddenException("Insufficient workspace permissions");
        }
        return member;
    }
}
