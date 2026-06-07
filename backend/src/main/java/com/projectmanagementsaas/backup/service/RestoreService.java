package com.projectmanagementsaas.backup.service;

import com.projectmanagementsaas.backup.entity.BackupMetadata;
import com.projectmanagementsaas.backup.repository.BackupMetadataRepository;
import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestoreService {
    private final BackupMetadataRepository backupRepository;
    private final String restoreCommand;

    public RestoreService(
            BackupMetadataRepository backupRepository,
            @Value("${backup.pg-restore-command:}") String restoreCommand
    ) {
        this.backupRepository = backupRepository;
        this.restoreCommand = restoreCommand;
    }

    @Transactional
    public BackupMetadata restore(UUID backupId) {
        BackupMetadata backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new NotFoundException("Backup not found"));
        if (restoreCommand == null || restoreCommand.isBlank()) {
            throw new BadRequestException("Restore command is not configured");
        }
        try {
            Process process = new ProcessBuilder("/bin/sh", "-lc",
                    restoreCommand + " " + shellQuote(backup.getStoragePath()))
                    .redirectErrorStream(true)
                    .start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IOException("Restore command failed with exit code " + exit);
            }
            backup.markRestored();
            backup.setMessage("Restore completed");
            return backupRepository.save(backup);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new BadRequestException(exception.getMessage());
        }
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
