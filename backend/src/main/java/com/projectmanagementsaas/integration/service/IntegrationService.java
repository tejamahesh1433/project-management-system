package com.projectmanagementsaas.integration.service;

import com.projectmanagementsaas.auth.service.TokenHashService;
import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.integration.dto.CreateIntegrationRequest;
import com.projectmanagementsaas.integration.dto.IntegrationResponse;
import com.projectmanagementsaas.integration.dto.IntegrationTestResponse;
import com.projectmanagementsaas.integration.entity.ConnectionStatus;
import com.projectmanagementsaas.integration.entity.Integration;
import com.projectmanagementsaas.integration.entity.IntegrationConnection;
import com.projectmanagementsaas.integration.entity.IntegrationType;
import com.projectmanagementsaas.integration.mapper.IntegrationMapper;
import com.projectmanagementsaas.integration.repository.IntegrationConnectionRepository;
import com.projectmanagementsaas.integration.repository.IntegrationRepository;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.service.ProjectAccessService;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import com.projectmanagementsaas.webhook.entity.WebhookSubscription;
import com.projectmanagementsaas.webhook.repository.WebhookSubscriptionRepository;
import com.projectmanagementsaas.workspace.service.WorkspaceAccessService;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationService {
    private final IntegrationRepository integrationRepository;
    private final IntegrationConnectionRepository connectionRepository;
    private final WebhookSubscriptionRepository webhookRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final ProjectAccessService projectAccessService;
    private final IntegrationMapper mapper;
    private final TokenHashService tokenHashService;
    private final SecureRandom secureRandom = new SecureRandom();

    public IntegrationService(IntegrationRepository integrationRepository,
            IntegrationConnectionRepository connectionRepository,
            WebhookSubscriptionRepository webhookRepository,
            UserRepository userRepository,
            WorkspaceAccessService workspaceAccessService,
            ProjectAccessService projectAccessService,
            IntegrationMapper mapper,
            TokenHashService tokenHashService) {
        this.integrationRepository = integrationRepository;
        this.connectionRepository = connectionRepository;
        this.webhookRepository = webhookRepository;
        this.userRepository = userRepository;
        this.workspaceAccessService = workspaceAccessService;
        this.projectAccessService = projectAccessService;
        this.mapper = mapper;
        this.tokenHashService = tokenHashService;
    }

    @Transactional
    public IntegrationResponse create(UUID currentUserId, CreateIntegrationRequest request) {
        workspaceAccessService.requireMembership(request.workspaceId(), currentUserId);
        if (request.projectId() != null) {
            Project project = projectAccessService.requireProject(request.projectId());
            if (!project.getWorkspace().getId().equals(request.workspaceId())) {
                throw new BadRequestException("Project must belong to the integration workspace");
            }
            projectAccessService.requireProjectMember(request.projectId(), currentUserId);
        }
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new NotFoundException("User not found"));

        Integration integration = new Integration();
        integration.setWorkspaceId(request.workspaceId());
        integration.setProjectId(request.projectId());
        integration.setType(request.type());
        integration.setName(request.name().trim());
        integration.setRepositoryUrl(normalizeOptional(request.repositoryUrl()));
        integration.setRepositoryName(normalizeOptional(request.repositoryName()));
        integration.setMetadataJson(normalizeOptional(request.metadataJson()));
        integration.setCreatedBy(user);
        Integration saved = integrationRepository.save(integration);

        if (request.endpointUrl() != null && !request.endpointUrl().isBlank()) {
            IntegrationConnection connection = new IntegrationConnection();
            connection.setIntegration(saved);
            connection.setEndpointUrl(request.endpointUrl().trim());
            connection.setExternalId(request.repositoryName());
            connectionRepository.save(connection);
        }

        if (supportsWebhook(request.type())) {
            WebhookSubscription webhook = new WebhookSubscription();
            webhook.setIntegration(saved);
            webhook.setProvider(request.type().name());
            webhook.setSecretHash(tokenHashService.hash(randomSecret()));
            webhookRepository.save(webhook);
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<IntegrationResponse> list(UUID currentUserId, UUID workspaceId) {
        workspaceAccessService.requireMembership(workspaceId, currentUserId);
        return integrationRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public IntegrationResponse get(UUID currentUserId, UUID integrationId) {
        Integration integration = requireIntegration(integrationId);
        workspaceAccessService.requireMembership(integration.getWorkspaceId(), currentUserId);
        return toResponse(integration);
    }

    @Transactional
    public void delete(UUID currentUserId, UUID integrationId) {
        Integration integration = requireIntegration(integrationId);
        workspaceAccessService.requireMembership(integration.getWorkspaceId(), currentUserId);
        integrationRepository.delete(integration);
    }

    @Transactional
    public IntegrationTestResponse test(UUID currentUserId, UUID integrationId) {
        Integration integration = requireIntegration(integrationId);
        workspaceAccessService.requireMembership(integration.getWorkspaceId(), currentUserId);
        List<IntegrationConnection> connections = connectionRepository.findByIntegration_Id(integrationId);
        boolean success = !connections.isEmpty() && connections.stream()
                .allMatch(connection -> connection.getEndpointUrl() != null && !connection.getEndpointUrl().isBlank());
        String message = success ? "Connection metadata is configured" : "No connection endpoint configured";
        connections.forEach(connection -> connection.mark(success ? ConnectionStatus.CONNECTED : ConnectionStatus.FAILED, message));
        connectionRepository.saveAll(connections);
        integration.touch();
        integrationRepository.save(integration);
        return new IntegrationTestResponse(integrationId, success, message, Instant.now());
    }

    private Integration requireIntegration(UUID integrationId) {
        return integrationRepository.findById(integrationId).orElseThrow(() -> new NotFoundException("Integration not found"));
    }

    private IntegrationResponse toResponse(Integration integration) {
        return mapper.toResponse(integration, connectionRepository.findByIntegration_Id(integration.getId()),
                webhookRepository.findByIntegration_Id(integration.getId()));
    }

    private boolean supportsWebhook(IntegrationType type) {
        return type == IntegrationType.GITHUB || type == IntegrationType.GITLAB || type == IntegrationType.GITEA
                || type == IntegrationType.JENKINS;
    }

    private String randomSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
