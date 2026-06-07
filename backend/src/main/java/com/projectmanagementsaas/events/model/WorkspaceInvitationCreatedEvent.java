package com.projectmanagementsaas.events.model;

import java.util.UUID;

public record WorkspaceInvitationCreatedEvent(UUID invitationId, UUID workspaceId, String email, UUID actorId) {
}
