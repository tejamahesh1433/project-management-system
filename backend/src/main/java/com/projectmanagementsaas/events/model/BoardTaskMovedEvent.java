package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record BoardTaskMovedEvent(UUID boardId, UUID taskId, UUID columnId, int position, UUID actorId) {
}
