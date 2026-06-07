package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record FileUploadedEvent(UUID fileId, UUID projectId, UUID actorId) {
}
