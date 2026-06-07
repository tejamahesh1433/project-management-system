package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record BoardColumnCreatedEvent(UUID columnId, UUID boardId, UUID actorId) {
}
