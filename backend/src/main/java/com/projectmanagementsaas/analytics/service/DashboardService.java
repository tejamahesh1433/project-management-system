package com.projectmanagementsaas.analytics.service;

import com.projectmanagementsaas.analytics.dto.CreateDashboardRequest;
import com.projectmanagementsaas.analytics.dto.CreateWidgetRequest;
import com.projectmanagementsaas.analytics.dto.DashboardResponse;
import com.projectmanagementsaas.analytics.dto.DashboardWidgetResponse;
import com.projectmanagementsaas.analytics.dto.UpdateDashboardRequest;
import com.projectmanagementsaas.analytics.dto.UpdateWidgetRequest;
import com.projectmanagementsaas.analytics.entity.Dashboard;
import com.projectmanagementsaas.analytics.entity.DashboardWidget;
import com.projectmanagementsaas.analytics.mapper.DashboardMapper;
import com.projectmanagementsaas.analytics.repository.DashboardRepository;
import com.projectmanagementsaas.analytics.repository.DashboardWidgetRepository;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.events.model.DashboardCreatedEvent;
import com.projectmanagementsaas.events.model.DashboardUpdatedEvent;
import com.projectmanagementsaas.events.model.WidgetCreatedEvent;
import com.projectmanagementsaas.project.service.ProjectAccessService;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import com.projectmanagementsaas.workspace.service.WorkspaceAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final DashboardWidgetRepository widgetRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final ProjectAccessService projectAccessService;
    private final DashboardMapper dashboardMapper;
    private final ApplicationEventPublisher eventPublisher;

    public DashboardService(
            DashboardRepository dashboardRepository,
            DashboardWidgetRepository widgetRepository,
            UserRepository userRepository,
            WorkspaceAccessService workspaceAccessService,
            ProjectAccessService projectAccessService,
            DashboardMapper dashboardMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.dashboardRepository = dashboardRepository;
        this.widgetRepository = widgetRepository;
        this.userRepository = userRepository;
        this.workspaceAccessService = workspaceAccessService;
        this.projectAccessService = projectAccessService;
        this.dashboardMapper = dashboardMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DashboardResponse create(UUID currentUserId, CreateDashboardRequest request) {
        workspaceAccessService.requireMembership(request.workspaceId(), currentUserId);
        if (request.projectId() != null) {
            projectAccessService.requireProjectMember(request.projectId(), currentUserId);
        }
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Dashboard dashboard = new Dashboard();
        dashboard.setWorkspaceId(request.workspaceId());
        dashboard.setProjectId(request.projectId());
        dashboard.setName(request.name().trim());
        dashboard.setCreatedBy(user);
        Dashboard saved = dashboardRepository.save(dashboard);
        eventPublisher.publishEvent(new DashboardCreatedEvent(saved.getId(), saved.getWorkspaceId(), currentUserId));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DashboardResponse> list(UUID currentUserId, UUID workspaceId) {
        workspaceAccessService.requireMembership(workspaceId, currentUserId);
        return dashboardRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardResponse get(UUID currentUserId, UUID dashboardId) {
        Dashboard dashboard = requireDashboard(dashboardId);
        workspaceAccessService.requireMembership(dashboard.getWorkspaceId(), currentUserId);
        return toResponse(dashboard);
    }

    @Transactional
    public DashboardResponse update(UUID currentUserId, UUID dashboardId, UpdateDashboardRequest request) {
        Dashboard dashboard = requireDashboard(dashboardId);
        workspaceAccessService.requireMembership(dashboard.getWorkspaceId(), currentUserId);
        dashboard.setName(request.name().trim());
        dashboard.touch();
        Dashboard saved = dashboardRepository.save(dashboard);
        eventPublisher.publishEvent(new DashboardUpdatedEvent(saved.getId(), saved.getWorkspaceId(), currentUserId));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID currentUserId, UUID dashboardId) {
        Dashboard dashboard = requireDashboard(dashboardId);
        workspaceAccessService.requireMembership(dashboard.getWorkspaceId(), currentUserId);
        dashboardRepository.delete(dashboard);
    }

    @Transactional
    public DashboardWidgetResponse createWidget(UUID currentUserId, UUID dashboardId, CreateWidgetRequest request) {
        Dashboard dashboard = requireDashboard(dashboardId);
        workspaceAccessService.requireMembership(dashboard.getWorkspaceId(), currentUserId);
        DashboardWidget widget = new DashboardWidget();
        widget.setDashboard(dashboard);
        widget.setType(request.type());
        widget.setTitle(request.title().trim());
        widget.setPosition(request.position() == null ? 0 : request.position());
        widget.setConfigJson(normalizeOptional(request.configJson()));
        DashboardWidget saved = widgetRepository.save(widget);
        eventPublisher.publishEvent(new WidgetCreatedEvent(saved.getId(), dashboard.getId(), dashboard.getWorkspaceId(), currentUserId));
        return dashboardMapper.toWidgetResponse(saved);
    }

    @Transactional
    public DashboardWidgetResponse updateWidget(UUID currentUserId, UUID dashboardId, UUID widgetId, UpdateWidgetRequest request) {
        Dashboard dashboard = requireDashboard(dashboardId);
        workspaceAccessService.requireMembership(dashboard.getWorkspaceId(), currentUserId);
        DashboardWidget widget = widgetRepository.findByIdAndDashboard_Id(widgetId, dashboardId)
                .orElseThrow(() -> new NotFoundException("Widget not found"));
        widget.setType(request.type());
        widget.setTitle(request.title().trim());
        widget.setPosition(request.position() == null ? widget.getPosition() : request.position());
        widget.setConfigJson(normalizeOptional(request.configJson()));
        widget.touch();
        return dashboardMapper.toWidgetResponse(widgetRepository.save(widget));
    }

    @Transactional
    public void deleteWidget(UUID currentUserId, UUID dashboardId, UUID widgetId) {
        Dashboard dashboard = requireDashboard(dashboardId);
        workspaceAccessService.requireMembership(dashboard.getWorkspaceId(), currentUserId);
        DashboardWidget widget = widgetRepository.findByIdAndDashboard_Id(widgetId, dashboardId)
                .orElseThrow(() -> new NotFoundException("Widget not found"));
        widgetRepository.delete(widget);
    }

    private Dashboard requireDashboard(UUID dashboardId) {
        return dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new NotFoundException("Dashboard not found"));
    }

    private DashboardResponse toResponse(Dashboard dashboard) {
        return dashboardMapper.toResponse(dashboard, widgetRepository.findByDashboard_IdOrderByPositionAscCreatedAtAsc(dashboard.getId()));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
