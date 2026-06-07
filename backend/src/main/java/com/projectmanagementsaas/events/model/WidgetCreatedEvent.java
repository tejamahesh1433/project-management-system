package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record WidgetCreatedEvent(UUID widgetId, UUID dashboardId, UUID workspaceId, UUID actorId) {
}
