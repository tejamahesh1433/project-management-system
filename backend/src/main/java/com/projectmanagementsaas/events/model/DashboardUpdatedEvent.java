package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record DashboardUpdatedEvent(UUID dashboardId, UUID workspaceId, UUID actorId) {
}
