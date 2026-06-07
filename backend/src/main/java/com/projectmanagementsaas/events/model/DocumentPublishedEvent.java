package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record DocumentPublishedEvent(UUID documentId, UUID projectId, UUID actorId) {
}
