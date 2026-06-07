package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record SprintStartedEvent(UUID sprintId, UUID projectId, UUID actorId) {
}
