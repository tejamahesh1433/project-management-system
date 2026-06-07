package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record DocumentCreatedEvent(UUID documentId, UUID projectId, UUID actorId) {
}
