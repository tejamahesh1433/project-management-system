package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record SprintCancelledEvent(UUID sprintId, UUID projectId, UUID actorId) {
}
