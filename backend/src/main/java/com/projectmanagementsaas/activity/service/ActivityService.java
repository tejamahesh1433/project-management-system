package com.projectmanagementsaas.activity.service;

import com.projectmanagementsaas.activity.dto.ActivityResponse;
import com.projectmanagementsaas.activity.entity.Activity;
import com.projectmanagementsaas.activity.mapper.ActivityMapper;
import com.projectmanagementsaas.activity.repository.ActivityRepository;
import com.projectmanagementsaas.project.service.ProjectAccessService;
import com.projectmanagementsaas.workspace.service.WorkspaceAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final ActivityMapper mapper;
    private final WorkspaceAccessService workspaceAccessService;
    private final ProjectAccessService projectAccessService;

    public ActivityService(ActivityRepository activityRepository, ActivityMapper mapper,
            WorkspaceAccessService workspaceAccessService, ProjectAccessService projectAccessService) {
        this.activityRepository = activityRepository;
        this.mapper = mapper;
        this.workspaceAccessService = workspaceAccessService;
        this.projectAccessService = projectAccessService;
    }

    @Transactional
    public void record(UUID workspaceId, UUID projectId, UUID actorId, String action, String entityType, UUID entityId, String message) {
        Activity activity = new Activity();
        activity.setWorkspaceId(workspaceId);
        activity.setProjectId(projectId);
        activity.setActorId(actorId);
        activity.setAction(action);
        activity.setEntityType(entityType);
        activity.setEntityId(entityId);
        activity.setMessage(message);
        activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> userTimeline(UUID userId) {
        return activityRepository.findByActorIdOrderByCreatedAtDesc(userId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> workspaceFeed(UUID currentUserId, UUID workspaceId) {
        workspaceAccessService.requireMembership(workspaceId, currentUserId);
        return activityRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> projectFeed(UUID currentUserId, UUID projectId) {
        projectAccessService.requireProjectMember(projectId, currentUserId);
        return activityRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(mapper::toResponse).toList();
    }
}
