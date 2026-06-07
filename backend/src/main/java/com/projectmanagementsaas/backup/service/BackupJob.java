package com.projectmanagementsaas.backup.service;

import com.projectmanagementsaas.backup.entity.BackupMetadata;
import com.projectmanagementsaas.backup.entity.BackupStatus;
import com.projectmanagementsaas.backup.repository.BackupMetadataRepository;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BackupJob {
    private final BackupMetadataRepository backupRepository;
    private final UserRepository userRepository;
    private final Path backupDirectory;
    private final String backupCommand;

    public BackupJob(
            BackupMetadataRepository backupRepository,
            UserRepository userRepository,
            @Value("${backup.directory:./backups}") String backupDirectory,
            @Value("${backup.pg-dump-command:}") String backupCommand
    ) {
        this.backupRepository = backupRepository;
        this.userRepository = userRepository;
        this.backupDirectory = Path.of(backupDirectory);
        this.backupCommand = backupCommand;
    }

    @Transactional
    public BackupMetadata runManual(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        return runForUser(user);
    }

    @Transactional
    public BackupMetadata runScheduled(String systemUserEmail) {
        User user = userRepository.findByEmailIgnoreCase(systemUserEmail)
                .orElseThrow(() -> new NotFoundException("Backup system user not found"));
        return runForUser(user);
    }

    private BackupMetadata runForUser(User user) {
        String fileName = "postgres-" + DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-") + ".dump";
        Path target = backupDirectory.resolve(fileName);
        BackupMetadata metadata = new BackupMetadata();
        metadata.setFileName(fileName);
        metadata.setStoragePath(target.toAbsolutePath().normalize().toString());
        metadata.setCreatedBy(user);
        try {
            Files.createDirectories(backupDirectory);
            if (backupCommand == null || backupCommand.isBlank()) {
                Files.writeString(target, "Configure backup.pg-dump-command to enable PostgreSQL dump execution.\n");
                metadata.setMessage("Backup command not configured; metadata placeholder created");
            } else {
                runCommand(target);
                metadata.setMessage("Backup completed");
            }
            metadata.setSizeBytes(Files.size(target));
            metadata.setStatus(BackupStatus.COMPLETED);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            metadata.setStatus(BackupStatus.FAILED);
            metadata.setMessage(exception.getMessage());
            metadata.setSizeBytes(0);
        }
        return backupRepository.save(metadata);
    }

    private void runCommand(Path target) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("/bin/sh", "-lc", backupCommand + " > " + shellQuote(target.toString()))
                .redirectErrorStream(true)
                .start();
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IOException("Backup command failed with exit code " + exit);
        }
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
