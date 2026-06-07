package com.projectmanagementsaas.task.repository;

import com.projectmanagementsaas.task.entity.TaskComment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {
    Optional<TaskComment> findByIdAndDeletedAtIsNull(UUID id);

    List<TaskComment> findByTask_IdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID taskId);
}
