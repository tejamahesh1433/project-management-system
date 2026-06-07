package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record ProjectCreatedEvent(UUID projectId, UUID workspaceId, UUID actorId) {
}
