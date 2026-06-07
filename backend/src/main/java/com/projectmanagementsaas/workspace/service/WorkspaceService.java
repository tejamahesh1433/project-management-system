package com.projectmanagementsaas.workspace.service;

import com.projectmanagementsaas.auth.service.TokenHashService;
import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.common.exception.ForbiddenException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.events.model.WorkspaceInvitationCreatedEvent;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import com.projectmanagementsaas.workspace.dto.AcceptInvitationRequest;
import com.projectmanagementsaas.workspace.dto.CreateOrganizationRequest;
import com.projectmanagementsaas.workspace.dto.CreateWorkspaceRequest;
import com.projectmanagementsaas.workspace.dto.InviteWorkspaceMemberRequest;
import com.projectmanagementsaas.workspace.dto.OrganizationResponse;
import com.projectmanagementsaas.workspace.dto.UpdateWorkspaceMemberRoleRequest;
import com.projectmanagementsaas.workspace.dto.UpdateWorkspaceRequest;
import com.projectmanagementsaas.workspace.dto.WorkspaceInvitationResponse;
import com.projectmanagementsaas.workspace.dto.WorkspaceMemberResponse;
import com.projectmanagementsaas.workspace.dto.WorkspaceResponse;
import com.projectmanagementsaas.workspace.entity.InvitationStatus;
import com.projectmanagementsaas.workspace.entity.Organization;
import com.projectmanagementsaas.workspace.entity.Workspace;
import com.projectmanagementsaas.workspace.entity.WorkspaceInvitation;
import com.projectmanagementsaas.workspace.entity.WorkspaceMember;
import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import com.projectmanagementsaas.workspace.repository.OrganizationRepository;
import com.projectmanagementsaas.workspace.repository.WorkspaceInvitationRepository;
import com.projectmanagementsaas.workspace.repository.WorkspaceMemberRepository;
import com.projectmanagementsaas.workspace.repository.WorkspaceRepository;
import com.projectmanagementsaas.workspace.validator.SlugValidator;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceService {
    private static final Duration INVITATION_TTL = Duration.ofDays(7);
    private final OrganizationRepository organizationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final SlugValidator slugValidator;
    private final TokenHashService tokenHashService;
    private final ApplicationEventPublisher eventPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

    public WorkspaceService(
            OrganizationRepository organizationRepository,
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            WorkspaceInvitationRepository workspaceInvitationRepository,
            UserRepository userRepository,
            WorkspaceAccessService workspaceAccessService,
            SlugValidator slugValidator,
            TokenHashService tokenHashService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.organizationRepository = organizationRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceInvitationRepository = workspaceInvitationRepository;
        this.userRepository = userRepository;
        this.workspaceAccessService = workspaceAccessService;
        this.slugValidator = slugValidator;
        this.tokenHashService = tokenHashService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrganizationResponse createOrganization(UUID currentUserId, CreateOrganizationRequest request) {
        User owner = getUser(currentUserId);
        String slug = slugValidator.validate(request.slug());
        if (organizationRepository.existsBySlugIgnoreCase(slug)) {
            throw new BadRequestException("Organization slug is already in use");
        }

        Organization organization = new Organization();
        organization.setName(request.name().trim());
        organization.setSlug(slug);
        organization.setOwner(owner);

        return toOrganizationResponse(organizationRepository.save(organization));
    }

    @Transactional
    public WorkspaceResponse createWorkspace(UUID currentUserId, CreateWorkspaceRequest request) {
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        if (!organization.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("Only the organization owner can create workspaces");
        }

        String slug = slugValidator.validate(request.slug());
        if (workspaceRepository.existsByOrganization_IdAndSlugIgnoreCase(organization.getId(), slug)) {
            throw new BadRequestException("Workspace slug is already in use for this organization");
        }

        Workspace workspace = new Workspace();
        workspace.setOrganization(organization);
        workspace.setName(request.name().trim());
        workspace.setSlug(slug);
        workspace.setDescription(normalizeOptional(request.description()));
        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember ownerMember = new WorkspaceMember();
        ownerMember.setWorkspace(savedWorkspace);
        ownerMember.setUser(getUser(currentUserId));
        ownerMember.setRole(WorkspaceRole.OWNER);
        workspaceMemberRepository.save(ownerMember);

        return toWorkspaceResponse(savedWorkspace, WorkspaceRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> listWorkspaces(UUID currentUserId) {
        return workspaceMemberRepository.findByUser_Id(currentUserId).stream()
                .sorted(Comparator.comparing(member -> member.getWorkspace().getCreatedAt()))
                .map(member -> toWorkspaceResponse(member.getWorkspace(), member.getRole()))
                .toList();
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(UUID currentUserId, UUID workspaceId, UpdateWorkspaceRequest request) {
        WorkspaceMember currentMember = workspaceAccessService.requireRole(
                workspaceId,
                currentUserId,
                WorkspaceRole.OWNER,
                WorkspaceRole.ADMIN);
        Workspace workspace = currentMember.getWorkspace();
        String slug = slugValidator.validate(request.slug());

        if (!workspace.getSlug().equalsIgnoreCase(slug)
                && workspaceRepository.existsByOrganization_IdAndSlugIgnoreCase(workspace.getOrganization().getId(), slug)) {
            throw new BadRequestException("Workspace slug is already in use for this organization");
        }

        workspace.setName(request.name().trim());
        workspace.setSlug(slug);
        workspace.setDescription(normalizeOptional(request.description()));
        workspace.setUpdatedAt(Instant.now());
        return toWorkspaceResponse(workspaceRepository.save(workspace), currentMember.getRole());
    }

    @Transactional
    public void deleteWorkspace(UUID currentUserId, UUID workspaceId) {
        workspaceAccessService.requireRole(workspaceId, currentUserId, WorkspaceRole.OWNER);
        workspaceRepository.deleteById(workspaceId);
    }

    @Transactional
    public WorkspaceInvitationResponse inviteMember(UUID currentUserId, UUID workspaceId, InviteWorkspaceMemberRequest request) {
        WorkspaceMember inviter = workspaceAccessService.requireRole(
                workspaceId,
                currentUserId,
                WorkspaceRole.OWNER,
                WorkspaceRole.ADMIN);

        WorkspaceRole invitedRole = request.role();
        if (invitedRole == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot invite a member as OWNER");
        }

        String email = request.email().trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(email)
                .filter(user -> workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, user.getId()))
                .ifPresent(user -> {
                    throw new BadRequestException("User is already a workspace member");
                });

        if (workspaceInvitationRepository.existsByWorkspace_IdAndEmailIgnoreCaseAndStatus(
                workspaceId,
                email,
                InvitationStatus.PENDING)) {
            throw new BadRequestException("A pending invitation already exists for this email");
        }

        String rawToken = randomToken();
        WorkspaceInvitation invitation = new WorkspaceInvitation();
        invitation.setWorkspace(inviter.getWorkspace());
        invitation.setEmail(email);
        invitation.setRole(invitedRole);
        invitation.setTokenHash(tokenHashService.hash(rawToken));
        invitation.setInvitedBy(inviter.getUser());
        invitation.setExpiresAt(Instant.now().plus(INVITATION_TTL));

        WorkspaceInvitation savedInvitation = workspaceInvitationRepository.save(invitation);
        eventPublisher.publishEvent(new WorkspaceInvitationCreatedEvent(savedInvitation.getId(), workspaceId, email, currentUserId));
        return toInvitationResponse(savedInvitation, rawToken);
    }

    @Transactional
    public WorkspaceMemberResponse acceptInvitation(UUID currentUserId, AcceptInvitationRequest request) {
        User user = getUser(currentUserId);
        WorkspaceInvitation invitation = workspaceInvitationRepository.findByTokenHash(tokenHashService.hash(request.token()))
                .orElseThrow(() -> new BadRequestException("Invalid invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING || invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid invitation token");
        }
        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new ForbiddenException("Invitation belongs to a different email address");
        }
        if (workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(invitation.getWorkspace().getId(), currentUserId)) {
            throw new BadRequestException("User is already a workspace member");
        }

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(invitation.getWorkspace());
        member.setUser(user);
        member.setRole(invitation.getRole());
        WorkspaceMember savedMember = workspaceMemberRepository.save(member);
        invitation.accept();
        workspaceInvitationRepository.save(invitation);

        return toMemberResponse(savedMember);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> listMembers(UUID currentUserId, UUID workspaceId) {
        workspaceAccessService.requireMembership(workspaceId, currentUserId);
        return workspaceMemberRepository.findByWorkspace_Id(workspaceId).stream()
                .sorted(Comparator.comparing(member -> member.getUser().getEmail()))
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public WorkspaceMemberResponse updateMemberRole(
            UUID currentUserId,
            UUID workspaceId,
            UUID memberId,
            UpdateWorkspaceMemberRoleRequest request
    ) {
        workspaceAccessService.requireRole(workspaceId, currentUserId, WorkspaceRole.OWNER, WorkspaceRole.ADMIN);
        WorkspaceMember targetMember = workspaceMemberRepository.findById(memberId)
                .filter(member -> member.getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new NotFoundException("Workspace member not found"));

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot change the OWNER role");
        }
        if (request.role() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot assign OWNER role through role management");
        }

        targetMember.setRole(request.role());
        targetMember.setUpdatedAt(Instant.now());
        return toMemberResponse(workspaceMemberRepository.save(targetMember));
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> listOrganizations(UUID currentUserId) {
        return organizationRepository.findByOwner_Id(currentUserId).stream()
                .sorted(Comparator.comparing(Organization::getCreatedAt))
                .map(this::toOrganizationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganization(UUID currentUserId, UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
        if (!organization.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("Only the organization owner can access this organization");
        }
        return toOrganizationResponse(organization);
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(UUID currentUserId, UUID workspaceId) {
        WorkspaceMember member = workspaceAccessService.requireMembership(workspaceId, currentUserId);
        return toWorkspaceResponse(member.getWorkspace(), member.getRole());
    }

    @Transactional
    public void removeMember(UUID currentUserId, UUID workspaceId, UUID memberId) {
        WorkspaceMember currentMember = workspaceAccessService.requireMembership(workspaceId, currentUserId);
        WorkspaceMember targetMember = workspaceMemberRepository.findById(memberId)
                .filter(m -> m.getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new NotFoundException("Workspace member not found"));

        if (!currentMember.getId().equals(targetMember.getId())) {
            workspaceAccessService.requireRole(workspaceId, currentUserId, WorkspaceRole.OWNER, WorkspaceRole.ADMIN);
        }

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            long ownerCount = workspaceMemberRepository.countByWorkspace_IdAndRoleIn(workspaceId, List.of(WorkspaceRole.OWNER));
            if (ownerCount <= 1) {
                throw new BadRequestException("Cannot remove the last OWNER of the workspace");
            }
        }

        workspaceMemberRepository.delete(targetMember);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceInvitationResponse> listPendingInvitations(UUID currentUserId, UUID workspaceId) {
        workspaceAccessService.requireRole(workspaceId, currentUserId, WorkspaceRole.OWNER, WorkspaceRole.ADMIN);
        return workspaceInvitationRepository.findByWorkspace_IdAndStatus(workspaceId, InvitationStatus.PENDING).stream()
                .sorted(Comparator.comparing(WorkspaceInvitation::getCreatedAt).reversed())
                .map(invitation -> toInvitationResponse(invitation, null))
                .toList();
    }

    @Transactional
    public void revokeInvitation(UUID currentUserId, UUID workspaceId, UUID invitationId) {
        workspaceAccessService.requireRole(workspaceId, currentUserId, WorkspaceRole.OWNER, WorkspaceRole.ADMIN);
        WorkspaceInvitation invitation = workspaceInvitationRepository.findById(invitationId)
                .filter(inv -> inv.getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Only PENDING invitations can be revoked");
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        workspaceInvitationRepository.save(invitation);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private OrganizationResponse toOrganizationResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.getOwner().getId(),
                organization.getCreatedAt(),
                organization.getUpdatedAt());
    }

    private WorkspaceResponse toWorkspaceResponse(Workspace workspace, WorkspaceRole currentUserRole) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getOrganization().getId(),
                workspace.getName(),
                workspace.getSlug(),
                workspace.getDescription(),
                currentUserRole,
                workspace.getCreatedAt(),
                workspace.getUpdatedAt());
    }

    private WorkspaceInvitationResponse toInvitationResponse(WorkspaceInvitation invitation, String rawToken) {
        return new WorkspaceInvitationResponse(
                invitation.getId(),
                invitation.getWorkspace().getId(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                rawToken);
    }

    private WorkspaceMemberResponse toMemberResponse(WorkspaceMember member) {
        return new WorkspaceMemberResponse(
                member.getId(),
                member.getWorkspace().getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getDisplayName(),
                member.getRole(),
                member.getCreatedAt());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
