package com.projectmanagementsaas.project.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.workspace.repository.WorkspaceMemberRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMembershipValidator {
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspaceMembershipValidator(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public void validateMember(UUID workspaceId, UUID userId) {
        if (!workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, userId)) {
            throw new BadRequestException("User must belong to the workspace before joining the project");
        }
    }
}
