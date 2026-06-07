package com.projectmanagementsaas.webhook.service;

import com.projectmanagementsaas.integration.entity.ConnectionStatus;
import com.projectmanagementsaas.integration.entity.IntegrationConnection;
import com.projectmanagementsaas.integration.entity.IntegrationType;
import com.projectmanagementsaas.integration.repository.IntegrationConnectionRepository;
import com.projectmanagementsaas.webhook.entity.WebhookSubscription;
import com.projectmanagementsaas.webhook.repository.WebhookSubscriptionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookHandlerService {
    private final WebhookSubscriptionRepository webhookRepository;
    private final IntegrationConnectionRepository connectionRepository;

    public WebhookHandlerService(WebhookSubscriptionRepository webhookRepository,
            IntegrationConnectionRepository connectionRepository) {
        this.webhookRepository = webhookRepository;
        this.connectionRepository = connectionRepository;
    }

    @Transactional
    public int handle(IntegrationType type, String payload) {
        List<WebhookSubscription> subscriptions = webhookRepository.findByIntegration_TypeAndEnabledTrue(type);
        subscriptions.forEach(WebhookSubscription::markReceived);
        subscriptions.forEach(subscription -> {
            List<IntegrationConnection> connections = connectionRepository.findByIntegration_Id(subscription.getIntegration().getId());
            connections.forEach(connection -> connection.mark(ConnectionStatus.CONNECTED,
                    type.name() + " webhook received: " + summarize(payload)));
            connectionRepository.saveAll(connections);
        });
        webhookRepository.saveAll(subscriptions);
        return subscriptions.size();
    }

    private String summarize(String payload) {
        if (payload == null || payload.isBlank()) {
            return "empty payload";
        }
        String normalized = payload.replaceAll("\\s+", " ").trim();
        return normalized.length() > 160 ? normalized.substring(0, 160) : normalized;
    }
}
