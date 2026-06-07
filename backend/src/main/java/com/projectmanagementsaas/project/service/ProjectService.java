package com.projectmanagementsaas.project.service;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.common.exception.ForbiddenException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.events.model.ProjectArchivedEvent;
import com.projectmanagementsaas.events.model.ProjectCreatedEvent;
import com.projectmanagementsaas.events.model.ProjectMemberAddedEvent;
import com.projectmanagementsaas.events.model.ProjectMemberRemovedEvent;
import com.projectmanagementsaas.events.model.ProjectRestoredEvent;
import com.projectmanagementsaas.events.model.ProjectUpdatedEvent;
import com.projectmanagementsaas.project.dto.AddProjectMemberRequest;
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.project.dto.ProjectMemberResponse;
import com.projectmanagementsaas.project.dto.ProjectResponse;
import com.projectmanagementsaas.project.dto.UpdateProjectMemberRoleRequest;
import com.projectmanagementsaas.project.dto.UpdateProjectRequest;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.entity.ProjectMember;
import com.projectmanagementsaas.project.entity.ProjectRole;
import com.projectmanagementsaas.project.entity.ProjectStatus;
import com.projectmanagementsaas.project.mapper.ProjectMapper;
import com.projectmanagementsaas.project.repository.ProjectMemberRepository;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.project.validator.ProjectRoleValidator;
import com.projectmanagementsaas.project.validator.UniqueProjectSlugValidator;
import com.projectmanagementsaas.project.validator.WorkspaceMembershipValidator;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import com.projectmanagementsaas.workspace.entity.Workspace;
import com.projectmanagementsaas.workspace.repository.WorkspaceRepository;
import com.projectmanagementsaas.workspace.validator.SlugValidator;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService projectAccessService;
    private final ProjectMapper projectMapper;
    private final SlugValidator slugValidator;
    private final UniqueProjectSlugValidator uniqueProjectSlugValidator;
    private final WorkspaceMembershipValidator workspaceMembershipValidator;
    private final ProjectRoleValidator projectRoleValidator;
    private final ApplicationEventPublisher eventPublisher;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            WorkspaceRepository workspaceRepository,
            UserRepository userRepository,
            ProjectAccessService projectAccessService,
            ProjectMapper projectMapper,
            SlugValidator slugValidator,
            UniqueProjectSlugValidator uniqueProjectSlugValidator,
            WorkspaceMembershipValidator workspaceMembershipValidator,
            ProjectRoleValidator projectRoleValidator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.projectAccessService = projectAccessService;
        this.projectMapper = projectMapper;
        this.slugValidator = slugValidator;
        this.uniqueProjectSlugValidator = uniqueProjectSlugValidator;
        this.workspaceMembershipValidator = workspaceMembershipValidator;
        this.projectRoleValidator = projectRoleValidator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ProjectResponse createProject(UUID currentUserId, CreateProjectRequest request) {
        Workspace workspace = workspaceRepository.findById(request.workspaceId())
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        projectAccessService.requireWorkspaceOwnerOrAdmin(workspace.getId(), currentUserId);
        String slug = slugValidator.validate(request.slug());
        uniqueProjectSlugValidator.validateUnique(workspace.getId(), null, slug);

        User creator = getUser(currentUserId);
        Project project = new Project();
        project.setWorkspace(workspace);
        project.setName(request.name().trim());
        project.setSlug(slug);
        project.setDescription(normalizeOptional(request.description()));
        project.setColor(normalizeOptional(request.color()));
        project.setIcon(normalizeOptional(request.icon()));
        project.setCreatedBy(creator);
        Project savedProject = projectRepository.save(project);

        ProjectMember owner = new ProjectMember();
        owner.setProject(savedProject);
        owner.setUser(creator);
        owner.setRole(ProjectRole.PROJECT_OWNER);
        projectMemberRepository.save(owner);

        eventPublisher.publishEvent(new ProjectCreatedEvent(savedProject.getId(), workspace.getId(), currentUserId));
        return projectMapper.toProjectResponse(savedProject, ProjectRole.PROJECT_OWNER);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(UUID currentUserId, UUID workspaceId) {
        workspaceMembershipValidator.validateMember(workspaceId, currentUserId);
        return projectRepository.findByWorkspace_IdAndDeletedAtIsNullOrderByCreatedAtAsc(workspaceId).stream()
                .filter(project -> projectMemberRepository.existsByProject_IdAndUser_Id(project.getId(), currentUserId))
                .map(project -> projectMapper.toProjectResponse(
                        project,
                        projectAccessService.requireProjectMember(project.getId(), currentUserId).getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID currentUserId, UUID projectId) {
        ProjectMember member = projectAccessService.requireProjectMember(projectId, currentUserId);
        return projectMapper.toProjectResponse(member.getProject(), member.getRole());
    }

    @Transactional
    public ProjectResponse updateProject(UUID currentUserId, UUID projectId, UpdateProjectRequest request) {
        ProjectMember currentMember = projectAccessService.requireProjectRole(
                projectId,
                currentUserId,
                ProjectRole.PROJECT_OWNER,
                ProjectRole.PROJECT_ADMIN);
        Project project = currentMember.getProject();
        String slug = slugValidator.validate(request.slug());
        uniqueProjectSlugValidator.validateUnique(project.getWorkspace().getId(), project.getSlug(), slug);

        project.setName(request.name().trim());
        project.setSlug(slug);
        project.setDescription(normalizeOptional(request.description()));
        project.setStatus(request.status() == null ? project.getStatus() : request.status());
        project.setColor(normalizeOptional(request.color()));
        project.setIcon(normalizeOptional(request.icon()));
        project.setUpdatedAt(Instant.now());
        Project savedProject = projectRepository.save(project);
        eventPublisher.publishEvent(new ProjectUpdatedEvent(projectId, project.getWorkspace().getId(), currentUserId));
        return projectMapper.toProjectResponse(savedProject, currentMember.getRole());
    }

    @Transactional
    public void softDeleteProject(UUID currentUserId, UUID projectId) {
        ProjectMember member = projectAccessService.requireProjectRole(projectId, currentUserId, ProjectRole.PROJECT_OWNER);
        member.getProject().softDelete();
        projectRepository.save(member.getProject());
    }

    @Transactional
    public ProjectResponse archiveProject(UUID currentUserId, UUID projectId) {
        ProjectMember member = projectAccessService.requireProjectRole(
                projectId,
                currentUserId,
                ProjectRole.PROJECT_OWNER,
                ProjectRole.PROJECT_ADMIN);
        Project project = member.getProject();
        project.setStatus(ProjectStatus.ARCHIVED);
        project.setUpdatedAt(Instant.now());
        Project savedProject = projectRepository.save(project);
        eventPublisher.publishEvent(new ProjectArchivedEvent(projectId, project.getWorkspace().getId(), currentUserId));
        return projectMapper.toProjectResponse(savedProject, member.getRole());
    }

    @Transactional
    public ProjectResponse restoreProject(UUID currentUserId, UUID projectId) {
        ProjectMember member = projectAccessService.requireProjectRole(
                projectId,
                currentUserId,
                ProjectRole.PROJECT_OWNER,
                ProjectRole.PROJECT_ADMIN);
        Project project = member.getProject();
        project.setStatus(ProjectStatus.ACTIVE);
        project.setUpdatedAt(Instant.now());
        Project savedProject = projectRepository.save(project);
        eventPublisher.publishEvent(new ProjectRestoredEvent(projectId, project.getWorkspace().getId(), currentUserId));
        return projectMapper.toProjectResponse(savedProject, member.getRole());
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(UUID currentUserId, UUID projectId) {
        projectAccessService.requireProjectMember(projectId, currentUserId);
        return projectMemberRepository.findByProject_Id(projectId).stream()
                .sorted(Comparator.comparing(member -> member.getUser().getEmail()))
                .map(projectMapper::toMemberResponse)
                .toList();
    }

    @Transactional
    public ProjectMemberResponse addMember(UUID currentUserId, UUID projectId, AddProjectMemberRequest request) {
        ProjectMember actor = requireMemberManager(projectId, currentUserId);
        projectRoleValidator.validateAssignable(request.role());
        workspaceMembershipValidator.validateMember(actor.getProject().getWorkspace().getId(), request.userId());
        if (projectMemberRepository.existsByProject_IdAndUser_Id(projectId, request.userId())) {
            throw new BadRequestException("User is already a project member");
        }

        User user = getUser(request.userId());
        ProjectMember member = new ProjectMember();
        member.setProject(actor.getProject());
        member.setUser(user);
        member.setRole(request.role());
        ProjectMember savedMember = projectMemberRepository.save(member);
        eventPublisher.publishEvent(new ProjectMemberAddedEvent(projectId, request.userId(), currentUserId));
        return projectMapper.toMemberResponse(savedMember);
    }

    @Transactional
    public ProjectMemberResponse updateMemberRole(
            UUID currentUserId,
            UUID projectId,
            UUID memberId,
            UpdateProjectMemberRoleRequest request
    ) {
        ProjectMember actor = requireMemberManager(projectId, currentUserId);
        projectRoleValidator.validateAssignable(request.role());
        ProjectMember target = getProjectMember(projectId, memberId);
        ensureCanManageTarget(actor, target);
        target.setRole(request.role());
        return projectMapper.toMemberResponse(projectMemberRepository.save(target));
    }

    @Transactional
    public void removeMember(UUID currentUserId, UUID projectId, UUID memberId) {
        ProjectMember actor = requireMemberManager(projectId, currentUserId);
        ProjectMember target = getProjectMember(projectId, memberId);
        ensureCanManageTarget(actor, target);
        projectMemberRepository.delete(target);
        eventPublisher.publishEvent(new ProjectMemberRemovedEvent(projectId, target.getUser().getId(), currentUserId));
    }

    private ProjectMember requireMemberManager(UUID projectId, UUID currentUserId) {
        return projectAccessService.requireProjectRole(
                projectId,
                currentUserId,
                ProjectRole.PROJECT_OWNER,
                ProjectRole.PROJECT_ADMIN);
    }

    private void ensureCanManageTarget(ProjectMember actor, ProjectMember target) {
        if (target.getRole() == ProjectRole.PROJECT_OWNER) {
            throw new BadRequestException("Cannot manage PROJECT_OWNER membership");
        }
        if (actor.getRole() == ProjectRole.PROJECT_ADMIN && target.getRole() == ProjectRole.PROJECT_ADMIN) {
            throw new ForbiddenException("PROJECT_ADMIN cannot manage another PROJECT_ADMIN");
        }
    }

    private ProjectMember getProjectMember(UUID projectId, UUID memberId) {
        return projectMemberRepository.findById(memberId)
                .filter(member -> member.getProject().getId().equals(projectId))
                .orElseThrow(() -> new NotFoundException("Project member not found"));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
