package com.projectmanagementsaas.sprint.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.sprint.entity.SprintStatus;
import com.projectmanagementsaas.sprint.repository.SprintRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ActiveSprintValidator {
    private final SprintRepository sprintRepository;

    public ActiveSprintValidator(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }

    public void validateNoActiveSprint(UUID projectId) {
        if (sprintRepository.existsByProject_IdAndStatusAndDeletedAtIsNull(projectId, SprintStatus.ACTIVE)) {
            throw new BadRequestException("Project already has an active sprint");
        }
    }
}
