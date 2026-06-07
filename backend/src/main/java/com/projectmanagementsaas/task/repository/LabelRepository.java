package com.projectmanagementsaas.task.repository;

import com.projectmanagementsaas.task.entity.Label;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, UUID> {
    boolean existsByProject_IdAndNameIgnoreCase(UUID projectId, String name);

    List<Label> findByProject_IdOrderByNameAsc(UUID projectId);

    Optional<Label> findByIdAndProject_Id(UUID id, UUID projectId);
}
