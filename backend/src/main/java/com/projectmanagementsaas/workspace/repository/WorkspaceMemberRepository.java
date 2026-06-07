package com.projectmanagementsaas.workspace.repository;

import com.projectmanagementsaas.workspace.entity.WorkspaceMember;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {
    Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);

    boolean existsByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);

    List<WorkspaceMember> findByUser_Id(UUID userId);

    List<WorkspaceMember> findByWorkspace_Id(UUID workspaceId);

    long countByWorkspace_IdAndRoleIn(UUID workspaceId, Collection<com.projectmanagementsaas.workspace.entity.WorkspaceRole> roles);
}
