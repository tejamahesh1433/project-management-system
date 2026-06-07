package com.projectmanagementsaas.integration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integration_connections")
public class IntegrationConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "integration_id", nullable = false)
    private Integration integration;

    @Column(nullable = false, length = 500)
    private String endpointUrl;

    @Column(length = 120)
    private String externalId;

    @Column(nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private ConnectionStatus status = ConnectionStatus.NOT_TESTED;

    @Column(length = 1000)
    private String lastMessage;

    @Column
    private Instant lastCheckedAt;

    public UUID getId() { return id; }
    public Integration getIntegration() { return integration; }
    public void setIntegration(Integration integration) { this.integration = integration; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public ConnectionStatus getStatus() { return status; }
    public void setStatus(ConnectionStatus status) { this.status = status; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public void mark(ConnectionStatus status, String message) {
        this.status = status;
        this.lastMessage = message;
        this.lastCheckedAt = Instant.now();
    }
}
