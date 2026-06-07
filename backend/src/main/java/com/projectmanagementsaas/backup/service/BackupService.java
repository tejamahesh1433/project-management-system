package com.projectmanagementsaas.backup.service;

import com.projectmanagementsaas.backup.dto.BackupResponse;
import com.projectmanagementsaas.backup.entity.BackupMetadata;
import com.projectmanagementsaas.backup.mapper.BackupMapper;
import com.projectmanagementsaas.backup.repository.BackupMetadataRepository;
import com.projectmanagementsaas.common.exception.NotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BackupService {
    private final BackupJob backupJob;
    private final RestoreService restoreService;
    private final BackupMetadataRepository backupRepository;
    private final BackupMapper backupMapper;
    private final String scheduledBackupUserEmail;

    public BackupService(BackupJob backupJob, RestoreService restoreService,
            BackupMetadataRepository backupRepository, BackupMapper backupMapper,
            @Value("${backup.system-user-email:}") String scheduledBackupUserEmail) {
        this.backupJob = backupJob;
        this.restoreService = restoreService;
        this.backupRepository = backupRepository;
        this.backupMapper = backupMapper;
        this.scheduledBackupUserEmail = scheduledBackupUserEmail;
    }

    public BackupResponse create(UUID currentUserId) {
        return backupMapper.toResponse(backupJob.runManual(currentUserId));
    }

    @Transactional(readOnly = true)
    public List<BackupResponse> history() {
        return backupRepository.findAllByOrderByCreatedAtDesc().stream().map(backupMapper::toResponse).toList();
    }

    public BackupResponse restore(UUID backupId) {
        return backupMapper.toResponse(restoreService.restore(backupId));
    }

    @Transactional(readOnly = true)
    public Path downloadPath(UUID backupId) {
        BackupMetadata backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new NotFoundException("Backup not found"));
        return Path.of(backup.getStoragePath()).toAbsolutePath().normalize();
    }

    @Scheduled(cron = "${backup.schedule-cron:0 0 2 * * *}")
    public void scheduledBackup() {
        if (scheduledBackupUserEmail != null && !scheduledBackupUserEmail.isBlank()) {
            backupJob.runScheduled(scheduledBackupUserEmail);
        }
    }
}
