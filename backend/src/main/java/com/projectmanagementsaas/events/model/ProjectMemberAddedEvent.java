package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record ProjectMemberAddedEvent(UUID projectId, UUID userId, UUID actorId) {
}
