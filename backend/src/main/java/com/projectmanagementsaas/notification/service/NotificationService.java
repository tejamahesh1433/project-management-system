package com.projectmanagementsaas.notification.service;

import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.notification.dto.*;
import com.projectmanagementsaas.notification.entity.*;
import com.projectmanagementsaas.notification.mapper.NotificationMapper;
import com.projectmanagementsaas.notification.repository.*;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final NotificationMapper mapper;

    public NotificationService(NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository, UserRepository userRepository, NotificationMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public void create(UUID userId, NotificationType type, String title, String message, String entityType,
            UUID entityId, UUID workspaceId, UUID projectId) {
        if (!isEnabled(userId, type)) return;
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notification.setWorkspaceId(workspaceId);
        notification.setProjectId(projectId);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(UUID userId) {
        return notificationRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unread(UUID userId) {
        return new UnreadCountResponse(notificationRepository.countByUser_IdAndReadAtIsNullAndDeletedAtIsNull(userId));
    }

    @Transactional
    public NotificationResponse markRead(UUID userId, UUID id) {
        Notification notification = notificationRepository.findByIdAndUser_IdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        notification.markRead();
        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(UUID userId) {
        List<Notification> unread = notificationRepository.findByUser_IdAndReadAtIsNullAndDeletedAtIsNull(userId);
        unread.forEach(Notification::markRead);
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Notification notification = notificationRepository.findByIdAndUser_IdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        notification.softDelete();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceItem> preferences(UUID userId) {
        Map<NotificationType, Boolean> existing = new EnumMap<>(NotificationType.class);
        preferenceRepository.findByUser_Id(userId).forEach(pref -> existing.put(pref.getType(), pref.isInAppEnabled()));
        return Arrays.stream(NotificationType.values())
                .map(type -> new NotificationPreferenceItem(type, existing.getOrDefault(type, true)))
                .toList();
    }

    @Transactional
    public List<NotificationPreferenceItem> updatePreferences(UUID userId, NotificationPreferencesRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        for (NotificationPreferenceItem item : request.preferences()) {
            NotificationPreference pref = preferenceRepository.findByUser_IdAndType(userId, item.type()).orElseGet(() -> {
                NotificationPreference created = new NotificationPreference();
                created.setUser(user);
                created.setType(item.type());
                return created;
            });
            pref.setInAppEnabled(item.inAppEnabled());
            pref.touch();
            preferenceRepository.save(pref);
        }
        return preferences(userId);
    }

    private boolean isEnabled(UUID userId, NotificationType type) {
        return preferenceRepository.findByUser_IdAndType(userId, type)
                .map(NotificationPreference::isInAppEnabled)
                .orElse(true);
    }
}
