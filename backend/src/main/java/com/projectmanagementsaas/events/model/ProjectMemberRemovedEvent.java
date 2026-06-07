package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record ProjectMemberRemovedEvent(UUID projectId, UUID userId, UUID actorId) {
}
