package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record SprintTaskRemovedEvent(UUID sprintId, UUID taskId, UUID actorId) {
}
