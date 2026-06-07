package com.projectmanagementsaas.project.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.project.entity.ProjectRole;
import org.springframework.stereotype.Component;

@Component
public class ProjectRoleValidator {
    public void validateAssignable(ProjectRole role) {
        if (role == ProjectRole.PROJECT_OWNER) {
            throw new BadRequestException("Cannot assign PROJECT_OWNER through member management");
        }
    }
}
