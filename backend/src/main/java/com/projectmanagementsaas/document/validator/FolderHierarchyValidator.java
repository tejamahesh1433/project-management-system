package com.projectmanagementsaas.document.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.document.entity.Folder;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FolderHierarchyValidator {
    public void validateParent(Folder parent, UUID projectId, UUID currentFolderId) {
        if (parent == null) return;
        if (!parent.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Parent folder must belong to the same project");
        }
        if (currentFolderId != null && parent.getId().equals(currentFolderId)) {
            throw new BadRequestException("Folder cannot be its own parent");
        }
    }
}
