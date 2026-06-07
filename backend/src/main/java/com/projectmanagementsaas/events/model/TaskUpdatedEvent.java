package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record TaskUpdatedEvent(UUID taskId, UUID projectId, UUID actorId) {
}
