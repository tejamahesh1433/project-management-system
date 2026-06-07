package com.projectmanagementsaas.webhook.repository;

import com.projectmanagementsaas.integration.entity.IntegrationType;
import com.projectmanagementsaas.webhook.entity.WebhookSubscription;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, UUID> {
    List<WebhookSubscription> findByIntegration_Id(UUID integrationId);

    List<WebhookSubscription> findByProviderIgnoreCaseAndEnabledTrue(String provider);

    List<WebhookSubscription> findByIntegration_TypeAndEnabledTrue(IntegrationType type);
}
