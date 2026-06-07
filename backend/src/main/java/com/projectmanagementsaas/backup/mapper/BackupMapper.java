package com.projectmanagementsaas.backup.mapper;

import com.projectmanagementsaas.backup.dto.BackupResponse;
import com.projectmanagementsaas.backup.entity.BackupMetadata;
import org.springframework.stereotype.Component;

@Component
public class BackupMapper {
    public BackupResponse toResponse(BackupMetadata backup) {
        return new BackupResponse(
                backup.getId(),
                backup.getFileName(),
                backup.getSizeBytes(),
                backup.getStatus(),
                backup.getMessage(),
                backup.getCreatedBy().getId(),
                backup.getCreatedAt(),
                backup.getRestoredAt());
    }
}
