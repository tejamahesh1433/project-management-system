package com.projectmanagementsaas.document.mapper;

import com.projectmanagementsaas.document.dto.DocumentResponse;
import com.projectmanagementsaas.document.dto.DocumentVersionResponse;
import com.projectmanagementsaas.document.dto.FolderResponse;
import com.projectmanagementsaas.document.entity.Document;
import com.projectmanagementsaas.document.entity.DocumentVersion;
import com.projectmanagementsaas.document.entity.Folder;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public FolderResponse toFolderResponse(Folder folder) {
        return new FolderResponse(folder.getId(), folder.getProject().getId(),
                folder.getParentFolder() == null ? null : folder.getParentFolder().getId(),
                folder.getName(), folder.getCreatedAt(), folder.getUpdatedAt());
    }

    public DocumentResponse toDocumentResponse(Document document) {
        return new DocumentResponse(document.getId(), document.getProject().getId(),
                document.getFolder() == null ? null : document.getFolder().getId(),
                document.getTitle(), document.getContent(), document.getStatus(), document.getCurrentVersion(),
                document.getCreatedBy().getId(), document.getCreatedAt(), document.getUpdatedAt());
    }

    public DocumentVersionResponse toVersionResponse(DocumentVersion version) {
        return new DocumentVersionResponse(version.getId(), version.getDocument().getId(), version.getVersionNumber(),
                version.getTitle(), version.getContent(), version.getCreatedBy().getId(), version.getCreatedAt());
    }
}
