package com.projectmanagementsaas.notification.entity;

import com.projectmanagementsaas.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 60)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 80)
    private String entityType;

    @Column
    private UUID entityId;

    @Column
    private UUID workspaceId;

    @Column
    private UUID projectId;

    @Column
    private Instant readAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant deletedAt;

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public Instant getReadAt() { return readAt; }
    public void markRead() { this.readAt = Instant.now(); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void softDelete() { this.deletedAt = Instant.now(); }
}
