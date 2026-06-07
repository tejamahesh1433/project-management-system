package com.projectmanagementsaas.audit.mapper;

import com.projectmanagementsaas.audit.dto.AuditLogResponse;
import com.projectmanagementsaas.audit.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {
    public AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getWorkspaceId(), log.getProjectId(), log.getActorId(),
                log.getAction(), log.getEntityType(), log.getEntityId(), log.getBeforeValue(), log.getAfterValue(),
                log.getIpAddress(), log.getUserAgent(), log.getCreatedAt());
    }
}
