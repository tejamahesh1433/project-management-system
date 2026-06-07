package com.projectmanagementsaas.task.repository;

import com.projectmanagementsaas.task.entity.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Optional<Task> findByIdAndDeletedAtIsNull(UUID id);

    List<Task> findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID projectId);
}
