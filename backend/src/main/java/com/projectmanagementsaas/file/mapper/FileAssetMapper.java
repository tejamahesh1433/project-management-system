package com.projectmanagementsaas.file.mapper;

import com.projectmanagementsaas.file.dto.FileAssetResponse;
import com.projectmanagementsaas.file.entity.FileAsset;
import org.springframework.stereotype.Component;

@Component
public class FileAssetMapper {
    public FileAssetResponse toResponse(FileAsset file) {
        return new FileAssetResponse(file.getId(), file.getProject().getId(),
                file.getFolder() == null ? null : file.getFolder().getId(),
                file.getFileName(), file.getStoragePath(), file.getContentType(), file.getSizeBytes(),
                file.getUploadedBy().getId(), file.getCreatedAt());
    }
}
