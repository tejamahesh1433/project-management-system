package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record ProjectUpdatedEvent(UUID projectId, UUID workspaceId, UUID actorId) {
}
