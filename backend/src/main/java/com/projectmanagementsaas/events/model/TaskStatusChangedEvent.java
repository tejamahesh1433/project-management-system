package com.projectmanagementsaas.events.model;

import com.projectmanagementsaas.task.entity.TaskStatus;
import java.util.UUID;

public record TaskStatusChangedEvent(UUID taskId, TaskStatus status, UUID actorId) {
}
