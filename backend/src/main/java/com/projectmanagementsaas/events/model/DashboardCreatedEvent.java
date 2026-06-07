package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record DashboardCreatedEvent(UUID dashboardId, UUID workspaceId, UUID actorId) {
}
