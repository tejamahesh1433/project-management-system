package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record DocumentVersionCreatedEvent(UUID documentId, int versionNumber, UUID actorId) {
}
