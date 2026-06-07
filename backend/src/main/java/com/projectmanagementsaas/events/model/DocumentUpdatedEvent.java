package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record DocumentUpdatedEvent(UUID documentId, UUID projectId, UUID actorId) {
}
