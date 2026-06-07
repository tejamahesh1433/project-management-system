package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record ProjectArchivedEvent(UUID projectId, UUID workspaceId, UUID actorId) {
}
