package com.projectmanagementsaas.webhook.dto;

public record WebhookResponse(
        String provider,
        String message
) {
}
