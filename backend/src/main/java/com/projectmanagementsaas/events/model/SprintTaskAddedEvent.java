package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record SprintTaskAddedEvent(UUID sprintId, UUID taskId, UUID actorId) {
}
