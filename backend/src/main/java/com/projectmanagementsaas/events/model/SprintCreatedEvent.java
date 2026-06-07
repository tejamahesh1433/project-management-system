package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record SprintCreatedEvent(UUID sprintId, UUID projectId, UUID actorId) {
}
