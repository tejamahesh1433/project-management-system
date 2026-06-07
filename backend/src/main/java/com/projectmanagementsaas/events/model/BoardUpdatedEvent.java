package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record BoardUpdatedEvent(UUID boardId, UUID projectId, UUID actorId) {
}
