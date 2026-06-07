package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record SprintUpdatedEvent(UUID sprintId, UUID projectId, UUID actorId) {
}
