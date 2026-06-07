package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record FileDeletedEvent(UUID fileId, UUID projectId, UUID actorId) {
}
