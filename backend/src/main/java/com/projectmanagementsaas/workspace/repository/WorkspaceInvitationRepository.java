package com.projectmanagementsaas.workspace.repository;

import com.projectmanagementsaas.workspace.entity.InvitationStatus;
import com.projectmanagementsaas.workspace.entity.WorkspaceInvitation;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID> {
    Optional<WorkspaceInvitation> findByTokenHash(String tokenHash);

    boolean existsByWorkspace_IdAndEmailIgnoreCaseAndStatus(UUID workspaceId, String email, InvitationStatus status);

    List<WorkspaceInvitation> findByWorkspace_IdAndStatus(UUID workspaceId, InvitationStatus status);
}
