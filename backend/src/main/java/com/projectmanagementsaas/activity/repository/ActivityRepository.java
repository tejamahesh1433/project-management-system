package com.projectmanagementsaas.activity.repository;

import com.projectmanagementsaas.activity.entity.Activity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findByActorIdOrderByCreatedAtDesc(UUID actorId);
    List<Activity> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);
    List<Activity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
