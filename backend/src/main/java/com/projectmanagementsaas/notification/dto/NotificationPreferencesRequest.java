package com.projectmanagementsaas.notification.dto;

import jakarta.validation.Valid;
import java.util.List;

public record NotificationPreferencesRequest(@Valid List<NotificationPreferenceItem> preferences) {
}
