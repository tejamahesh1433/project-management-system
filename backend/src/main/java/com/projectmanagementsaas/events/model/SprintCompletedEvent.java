package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record SprintCompletedEvent(UUID sprintId, UUID projectId, UUID actorId) {
}
