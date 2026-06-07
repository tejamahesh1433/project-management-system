package com.projectmanagementsaas.ai.entity;

import com.projectmanagementsaas.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_conversations")
public class AiConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID workspaceId;

    @Column
    private UUID projectId;

    @Column(nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private AiConversationScope scope;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private AiModel model;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public AiConversationScope getScope() { return scope; }
    public void setScope(AiConversationScope scope) { this.scope = scope; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public AiModel getModel() { return model; }
    public void setModel(AiModel model) { this.model = model; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void touch() { this.updatedAt = Instant.now(); }
}
