package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record AnalyticsViewedEvent(String scope, UUID scopeId, UUID actorId) {
}
