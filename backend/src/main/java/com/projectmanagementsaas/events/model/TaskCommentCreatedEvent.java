package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record TaskCommentCreatedEvent(UUID taskId, UUID commentId, UUID actorId) {
}
