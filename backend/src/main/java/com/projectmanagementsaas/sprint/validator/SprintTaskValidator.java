package com.projectmanagementsaas.sprint.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.sprint.entity.Sprint;
import com.projectmanagementsaas.task.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class SprintTaskValidator {
    public void validateTaskBelongsToSprintProject(Sprint sprint, Task task) {
        if (!task.getProject().getId().equals(sprint.getProject().getId())) {
            throw new BadRequestException("Task must belong to the sprint project");
        }
    }
}
