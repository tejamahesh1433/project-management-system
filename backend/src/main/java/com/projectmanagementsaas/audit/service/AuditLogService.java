package com.projectmanagementsaas.audit.service;

import com.projectmanagementsaas.audit.dto.AuditLogResponse;
import com.projectmanagementsaas.audit.entity.AuditLog;
import com.projectmanagementsaas.audit.mapper.AuditLogMapper;
import com.projectmanagementsaas.audit.repository.AuditLogRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper mapper;

    public AuditLogService(AuditLogRepository auditLogRepository, AuditLogMapper mapper) {
        this.auditLogRepository = auditLogRepository;
        this.mapper = mapper;
    }

    @Transactional
    public void record(UUID workspaceId, UUID projectId, UUID actorId, String action, String entityType, UUID entityId,
            String beforeValue, String afterValue, String ipAddress, String userAgent) {
        AuditLog log = new AuditLog();
        log.setWorkspaceId(workspaceId);
        log.setProjectId(projectId);
        log.setActorId(actorId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setBeforeValue(beforeValue);
        log.setAfterValue(afterValue);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> userAudit(UUID userId) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(userId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> entityAudit(String entityType, UUID entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType.toUpperCase(), entityId)
                .stream().map(mapper::toResponse).toList();
    }
}
