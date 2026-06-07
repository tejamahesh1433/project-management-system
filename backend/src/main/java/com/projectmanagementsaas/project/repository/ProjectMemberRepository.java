package com.projectmanagementsaas.project.repository;

import com.projectmanagementsaas.project.entity.ProjectMember;
import com.projectmanagementsaas.project.entity.ProjectRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    Optional<ProjectMember> findByProject_IdAndUser_Id(UUID projectId, UUID userId);

    boolean existsByProject_IdAndUser_Id(UUID projectId, UUID userId);

    List<ProjectMember> findByProject_Id(UUID projectId);

    long countByProject_IdAndRoleIn(UUID projectId, Collection<ProjectRole> roles);
}
