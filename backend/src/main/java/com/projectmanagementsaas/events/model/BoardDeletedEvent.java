package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record BoardDeletedEvent(UUID boardId, UUID projectId, UUID actorId) {
}
