package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record BoardColumnDeletedEvent(UUID columnId, UUID boardId, UUID actorId) {
}
