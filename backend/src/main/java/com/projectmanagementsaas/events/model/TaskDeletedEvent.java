package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record TaskDeletedEvent(UUID taskId, UUID projectId, UUID actorId) {
}
