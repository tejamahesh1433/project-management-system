package com.projectmanagementsaas.notification.dto;

import com.projectmanagementsaas.notification.entity.NotificationType;

public record NotificationPreferenceItem(NotificationType type, boolean inAppEnabled) {
}
