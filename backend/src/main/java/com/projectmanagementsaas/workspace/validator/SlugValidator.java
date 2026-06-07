package com.projectmanagementsaas.workspace.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class SlugValidator {
    private static final String SLUG_PATTERN = "^[a-z0-9]+(?:-[a-z0-9]+)*$";

    public String validate(String slug) {
        String normalized = slug == null ? "" : slug.trim().toLowerCase();
        if (!normalized.matches(SLUG_PATTERN)) {
            throw new BadRequestException("Slug must contain lowercase letters, numbers, and single hyphens only");
        }
        return normalized;
    }
}
