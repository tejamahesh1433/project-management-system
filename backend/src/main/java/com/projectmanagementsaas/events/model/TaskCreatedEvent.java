package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record TaskCreatedEvent(UUID taskId, UUID projectId, UUID actorId) {
}
