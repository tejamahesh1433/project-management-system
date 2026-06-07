package com.projectmanagementsaas.sprint.repository;

import com.projectmanagementsaas.sprint.entity.Sprint;
import com.projectmanagementsaas.sprint.entity.SprintStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    List<Sprint> findByProject_IdAndDeletedAtIsNullOrderByStartDateAsc(UUID projectId);

    Optional<Sprint> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByProject_IdAndStatusAndDeletedAtIsNull(UUID projectId, SprintStatus status);
}
