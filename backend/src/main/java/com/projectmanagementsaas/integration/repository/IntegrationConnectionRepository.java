package com.projectmanagementsaas.integration.repository;

import com.projectmanagementsaas.integration.entity.IntegrationConnection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationConnectionRepository extends JpaRepository<IntegrationConnection, UUID> {
    List<IntegrationConnection> findByIntegration_Id(UUID integrationId);
}
