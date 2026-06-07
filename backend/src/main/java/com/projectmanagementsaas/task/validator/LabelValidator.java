package com.projectmanagementsaas.task.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.task.repository.LabelRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LabelValidator {
    private final LabelRepository labelRepository;

    public LabelValidator(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    public void validateUnique(UUID projectId, String name) {
        if (labelRepository.existsByProject_IdAndNameIgnoreCase(projectId, name)) {
            throw new BadRequestException("Label name is already in use for this project");
        }
    }
}
