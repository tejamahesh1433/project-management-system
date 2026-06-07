package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record ProjectRestoredEvent(UUID projectId, UUID workspaceId, UUID actorId) {
}
