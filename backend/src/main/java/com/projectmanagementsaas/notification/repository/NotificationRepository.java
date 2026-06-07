package com.projectmanagementsaas.notification.repository;

import com.projectmanagementsaas.notification.entity.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);
    long countByUser_IdAndReadAtIsNullAndDeletedAtIsNull(UUID userId);
    Optional<Notification> findByIdAndUser_IdAndDeletedAtIsNull(UUID id, UUID userId);
    List<Notification> findByUser_IdAndReadAtIsNullAndDeletedAtIsNull(UUID userId);
}
