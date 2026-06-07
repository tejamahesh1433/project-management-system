package com.projectmanagementsaas.sprint.repository;

import com.projectmanagementsaas.sprint.entity.SprintTask;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintTaskRepository extends JpaRepository<SprintTask, UUID> {
    List<SprintTask> findBySprint_IdOrderByAddedAtAsc(UUID sprintId);

    Optional<SprintTask> findBySprint_IdAndTask_Id(UUID sprintId, UUID taskId);

    boolean existsBySprint_IdAndTask_Id(UUID sprintId, UUID taskId);
}
