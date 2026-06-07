package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record TaskAssignedEvent(UUID taskId, UUID assigneeId, UUID actorId) {
}
