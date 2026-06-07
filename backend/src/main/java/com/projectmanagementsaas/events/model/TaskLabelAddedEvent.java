package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record TaskLabelAddedEvent(UUID taskId, UUID labelId, UUID actorId) {
}
