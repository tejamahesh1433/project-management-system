package com.projectmanagementsaas.analytics.dto;

import java.util.UUID;

public record WorkspaceAnalyticsResponse(
        UUID workspaceId,
        int projects,
        int tasks,
        int documents,
        int files,
        int members,
        int activities
) {
}
