package com.projectmanagementsaas.task.repository;

import com.projectmanagementsaas.task.entity.TaskLabel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskLabelRepository extends JpaRepository<TaskLabel, UUID> {
    boolean existsByTask_IdAndLabel_Id(UUID taskId, UUID labelId);

    Optional<TaskLabel> findByTask_IdAndLabel_Id(UUID taskId, UUID labelId);

    List<TaskLabel> findByTask_Id(UUID taskId);
}
