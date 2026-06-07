package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record BoardColumnUpdatedEvent(UUID columnId, UUID boardId, UUID actorId) {
}
