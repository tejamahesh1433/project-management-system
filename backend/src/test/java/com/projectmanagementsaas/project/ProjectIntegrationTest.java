package com.projectmanagementsaas.project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagementsaas.IntegrationTestBase;
import com.projectmanagementsaas.auth.dto.RegisterRequest;
import com.projectmanagementsaas.project.dto.AddProjectMemberRequest;
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.project.dto.UpdateProjectMemberRoleRequest;
import com.projectmanagementsaas.project.dto.UpdateProjectRequest;
import com.projectmanagementsaas.project.entity.ProjectRole;
import com.projectmanagementsaas.project.entity.ProjectStatus;
import com.projectmanagementsaas.workspace.dto.AcceptInvitationRequest;
import com.projectmanagementsaas.workspace.dto.CreateOrganizationRequest;
import com.projectmanagementsaas.workspace.dto.CreateWorkspaceRequest;
import com.projectmanagementsaas.workspace.dto.InviteWorkspaceMemberRequest;
import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class ProjectIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void projectOwnerCanCreateUpdateArchiveRestoreAndDeleteProject() throws Exception {
        UserSession owner = register("phase3-owner@example.com");
        UUID organizationId = createOrganization(owner.token(), "Phase3 Org", "phase3-org");
        UUID workspaceId = createWorkspace(owner.token(), organizationId, "Phase3 Workspace", "phase3-workspace");
        UUID projectId = createProject(owner.token(), workspaceId, "Roadmap", "roadmap");

        mockMvc.perform(get("/api/v1/projects")
                        .queryParam("workspaceId", workspaceId.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(projectId.toString()))
                .andExpect(jsonPath("$[0].currentUserRole").value("PROJECT_OWNER"));

        UpdateProjectRequest updateRequest = new UpdateProjectRequest(
                "Roadmap 2026",
                "roadmap-2026",
                "Updated roadmap",
                ProjectStatus.ACTIVE,
                "#16a34a",
                "target");

        mockMvc.perform(put("/api/v1/projects/{projectId}", projectId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Roadmap 2026"))
                .andExpect(jsonPath("$.slug").value("roadmap-2026"));

        mockMvc.perform(post("/api/v1/projects/{projectId}/archive", projectId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(post("/api/v1/projects/{projectId}/restore", projectId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(delete("/api/v1/projects/{projectId}", projectId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project deleted"));
    }

    @Test
    void projectOwnerCanAddChangeAndRemoveMember() throws Exception {
        UserSession owner = register("phase3-member-owner@example.com");
        UserSession member = register("phase3-member@example.com");
        UUID organizationId = createOrganization(owner.token(), "Member Org", "member-org");
        UUID workspaceId = createWorkspace(owner.token(), organizationId, "Member Workspace", "member-workspace");
        UUID projectId = createProject(owner.token(), workspaceId, "Launch", "launch");
        acceptWorkspaceInvitation(owner.token(), member.token(), workspaceId, member.email(), WorkspaceRole.MEMBER);

        AddProjectMemberRequest addRequest = new AddProjectMemberRequest(member.userId(), ProjectRole.PROJECT_MEMBER);
        String addResponse = mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PROJECT_MEMBER"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID projectMemberId = UUID.fromString(objectMapper.readTree(addResponse).get("id").asText());

        mockMvc.perform(patch("/api/v1/projects/{projectId}/members/{memberId}/role", projectId, projectMemberId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProjectMemberRoleRequest(ProjectRole.PROJECT_VIEWER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PROJECT_VIEWER"));

        mockMvc.perform(delete("/api/v1/projects/{projectId}/members/{memberId}", projectId, projectMemberId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project member removed"));
    }

    @Test
    void userMustBelongToWorkspaceBeforeJoiningProject() throws Exception {
        UserSession owner = register("phase3-workspace-owner@example.com");
        UserSession outsider = register("phase3-outsider@example.com");
        UUID organizationId = createOrganization(owner.token(), "Workspace Rule Org", "workspace-rule-org");
        UUID workspaceId = createWorkspace(owner.token(), organizationId, "Workspace Rule", "workspace-rule");
        UUID projectId = createProject(owner.token(), workspaceId, "Rules", "rules");

        AddProjectMemberRequest addRequest = new AddProjectMemberRequest(outsider.userId(), ProjectRole.PROJECT_MEMBER);
        mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void projectViewerIsReadOnly() throws Exception {
        UserSession owner = register("phase3-viewer-owner@example.com");
        UserSession viewer = register("phase3-viewer@example.com");
        UUID organizationId = createOrganization(owner.token(), "Viewer Project Org", "viewer-project-org");
        UUID workspaceId = createWorkspace(owner.token(), organizationId, "Viewer Project Workspace", "viewer-project-workspace");
        UUID projectId = createProject(owner.token(), workspaceId, "Viewer Project", "viewer-project");
        acceptWorkspaceInvitation(owner.token(), viewer.token(), workspaceId, viewer.email(), WorkspaceRole.MEMBER);
        addProjectMember(owner.token(), projectId, viewer.userId(), ProjectRole.PROJECT_VIEWER);

        mockMvc.perform(get("/api/v1/projects/{projectId}", projectId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()));

        UpdateProjectRequest updateRequest = new UpdateProjectRequest(
                "Viewer Update",
                "viewer-update",
                null,
                ProjectStatus.ACTIVE,
                null,
                null);

        mockMvc.perform(put("/api/v1/projects/{projectId}", projectId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyWorkspaceOwnerOrAdminCanCreateProject() throws Exception {
        UserSession owner = register("phase3-create-owner@example.com");
        UserSession member = register("phase3-create-member@example.com");
        UUID organizationId = createOrganization(owner.token(), "Create Rule Org", "create-rule-org");
        UUID workspaceId = createWorkspace(owner.token(), organizationId, "Create Rule", "create-rule");
        acceptWorkspaceInvitation(owner.token(), member.token(), workspaceId, member.email(), WorkspaceRole.MEMBER);

        CreateProjectRequest createRequest = new CreateProjectRequest(
                workspaceId,
                "Forbidden",
                "forbidden",
                null,
                null,
                null);

        mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(member.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    private UserSession register(String email) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "Password123!", "Test User");
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return new UserSession(
                email,
                UUID.fromString(json.get("user").get("id").asText()),
                json.get("accessToken").asText());
    }

    private UUID createOrganization(String token, String name, String slug) throws Exception {
        CreateOrganizationRequest request = new CreateOrganizationRequest(name, slug);
        String response = mockMvc.perform(post("/api/v1/organizations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID createWorkspace(String token, UUID organizationId, String name, String slug) throws Exception {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest(organizationId, name, slug, null);
        String response = mockMvc.perform(post("/api/v1/workspaces")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID createProject(String token, UUID workspaceId, String name, String slug) throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(workspaceId, name, slug, null, "#2563eb", "folder");
        String response = mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.currentUserRole").value("PROJECT_OWNER"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private void acceptWorkspaceInvitation(
            String ownerToken,
            String memberToken,
            UUID workspaceId,
            String email,
            WorkspaceRole role
    ) throws Exception {
        InviteWorkspaceMemberRequest inviteRequest = new InviteWorkspaceMemberRequest(email, role);
        String inviteResponse = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/invitations", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String invitationToken = objectMapper.readTree(inviteResponse).get("token").asText();

        mockMvc.perform(post("/api/v1/workspaces/invitations/accept")
                        .header(HttpHeaders.AUTHORIZATION, bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcceptInvitationRequest(invitationToken))))
                .andExpect(status().isOk());
    }

    private void addProjectMember(String ownerToken, UUID projectId, UUID userId, ProjectRole role) throws Exception {
        AddProjectMemberRequest addRequest = new AddProjectMemberRequest(userId, role);
        mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record UserSession(String email, UUID userId, String token) {
    }
}
