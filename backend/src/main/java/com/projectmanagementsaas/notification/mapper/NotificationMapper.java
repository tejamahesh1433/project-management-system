package com.projectmanagementsaas.notification.mapper;

import com.projectmanagementsaas.notification.dto.NotificationPreferenceItem;
import com.projectmanagementsaas.notification.dto.NotificationResponse;
import com.projectmanagementsaas.notification.entity.Notification;
import com.projectmanagementsaas.notification.entity.NotificationPreference;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.getEntityType(), notification.getEntityId(),
                notification.getWorkspaceId(), notification.getProjectId(), notification.getReadAt() != null,
                notification.getCreatedAt());
    }

    public NotificationPreferenceItem toPreferenceItem(NotificationPreference preference) {
        return new NotificationPreferenceItem(preference.getType(), preference.isInAppEnabled());
    }
}
