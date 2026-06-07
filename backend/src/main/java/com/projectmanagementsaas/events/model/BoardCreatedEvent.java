package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record BoardCreatedEvent(UUID boardId, UUID projectId, UUID actorId) {
}
