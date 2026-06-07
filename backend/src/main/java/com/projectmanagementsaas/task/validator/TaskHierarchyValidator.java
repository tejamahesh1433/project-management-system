package com.projectmanagementsaas.task.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.task.entity.Task;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TaskHierarchyValidator {
    public void validateParent(Task parentTask, UUID projectId, UUID currentTaskId) {
        if (parentTask == null) {
            return;
        }
        if (!parentTask.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Parent task must belong to the same project");
        }
        if (currentTaskId != null && parentTask.getId().equals(currentTaskId)) {
            throw new BadRequestException("Task cannot be its own parent");
        }
    }
}
