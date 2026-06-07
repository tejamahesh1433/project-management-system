package com.projectmanagementsaas.task.repository;

import com.projectmanagementsaas.task.entity.TaskAttachment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, UUID> {
}
