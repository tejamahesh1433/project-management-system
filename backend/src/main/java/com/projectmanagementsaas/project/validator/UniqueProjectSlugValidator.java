package com.projectmanagementsaas.project.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UniqueProjectSlugValidator {
    private final ProjectRepository projectRepository;

    public UniqueProjectSlugValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void validateUnique(UUID workspaceId, String currentSlug, String requestedSlug) {
        if (currentSlug != null && currentSlug.equalsIgnoreCase(requestedSlug)) {
            return;
        }
        if (projectRepository.existsByWorkspace_IdAndSlugIgnoreCaseAndDeletedAtIsNull(workspaceId, requestedSlug)) {
            throw new BadRequestException("Project slug is already in use for this workspace");
        }
    }
}
