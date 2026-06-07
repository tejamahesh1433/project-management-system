package com.projectmanagementsaas.notification.repository;

import com.projectmanagementsaas.notification.entity.NotificationPreference;
import com.projectmanagementsaas.notification.entity.NotificationType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {
    List<NotificationPreference> findByUser_Id(UUID userId);
    Optional<NotificationPreference> findByUser_IdAndType(UUID userId, NotificationType type);
}
