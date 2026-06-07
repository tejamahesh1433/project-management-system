package com.projectmanagementsaas.workspace;

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
import com.projectmanagementsaas.workspace.dto.AcceptInvitationRequest;
import com.projectmanagementsaas.workspace.dto.CreateOrganizationRequest;
import com.projectmanagementsaas.workspace.dto.CreateWorkspaceRequest;
import com.projectmanagementsaas.workspace.dto.InviteWorkspaceMemberRequest;
import com.projectmanagementsaas.workspace.dto.UpdateWorkspaceMemberRoleRequest;
import com.projectmanagementsaas.workspace.dto.UpdateWorkspaceRequest;
import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class WorkspaceIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ownerCanCreateListUpdateAndDeleteWorkspace() throws Exception {
        String ownerToken = registerAndGetToken("workspace-owner@example.com");
        UUID organizationId = createOrganization(ownerToken, "Owner Org", "owner-org");
        UUID workspaceId = createWorkspace(ownerToken, organizationId, "Engineering", "engineering");

        mockMvc.perform(get("/api/v1/workspaces")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(workspaceId.toString()))
                .andExpect(jsonPath("$[0].currentUserRole").value("OWNER"));

        UpdateWorkspaceRequest updateRequest = new UpdateWorkspaceRequest(
                "Engineering Team",
                "engineering-team",
                "Updated workspace");

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Engineering Team"))
                .andExpect(jsonPath("$.slug").value("engineering-team"));

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Workspace deleted"));
    }

    @Test
    void canFetchOrganizationsAndWorkspaces() throws Exception {
        String ownerToken = registerAndGetToken("org-owner@example.com");
        UUID organizationId = createOrganization(ownerToken, "Test Org", "test-org");
        UUID workspaceId = createWorkspace(ownerToken, organizationId, "Test WS", "test-ws");

        // List organizations
        mockMvc.perform(get("/api/v1/organizations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(organizationId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Org"));

        // Get single organization
        mockMvc.perform(get("/api/v1/organizations/{id}", organizationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(organizationId.toString()));

        // Get single workspace
        mockMvc.perform(get("/api/v1/workspaces/{id}", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workspaceId.toString()));
    }

    @Test
    void ownerCanInviteMemberAndManageWorkspaceRole() throws Exception {
        String ownerToken = registerAndGetToken("workspace-admin-owner@example.com");
        String memberToken = registerAndGetToken("workspace-member@example.com");
        UUID organizationId = createOrganization(ownerToken, "Invite Org", "invite-org");
        UUID workspaceId = createWorkspace(ownerToken, organizationId, "Product", "product");

        InviteWorkspaceMemberRequest inviteRequest = new InviteWorkspaceMemberRequest(
                "workspace-member@example.com",
                WorkspaceRole.MEMBER);

        String inviteResponse = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/invitations", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String invitationToken = objectMapper.readTree(inviteResponse).get("token").asText();
        mockMvc.perform(post("/api/v1/workspaces/invitations/accept")
                        .header(HttpHeaders.AUTHORIZATION, bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcceptInvitationRequest(invitationToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"));

        String membersResponse = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/members", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID memberId = findMemberIdByEmail(membersResponse, "workspace-member@example.com");
        mockMvc.perform(patch("/api/v1/workspaces/{workspaceId}/members/{memberId}/role", workspaceId, memberId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateWorkspaceMemberRoleRequest(WorkspaceRole.VIEWER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("VIEWER"));

        // Remove the member
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/members/{memberId}", workspaceId, memberId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk());
    }

    @Test
    void ownerCanManagePendingInvitations() throws Exception {
        String ownerToken = registerAndGetToken("invitation-admin@example.com");
        UUID organizationId = createOrganization(ownerToken, "Invite List Org", "invite-list-org");
        UUID workspaceId = createWorkspace(ownerToken, organizationId, "Invites WS", "invites-ws");

        // Invite a user
        InviteWorkspaceMemberRequest inviteRequest = new InviteWorkspaceMemberRequest("pending-user@example.com", WorkspaceRole.MEMBER);
        String inviteResponse = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/invitations", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String invitationId = objectMapper.readTree(inviteResponse).get("id").asText();

        // List pending invitations
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/invitations", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(invitationId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // Revoke the invitation
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/invitations/{invitationId}", workspaceId, invitationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk());

        // Verify invitation is no longer pending
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/invitations", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void viewerCannotUpdateWorkspace() throws Exception {
        String ownerToken = registerAndGetToken("workspace-viewer-owner@example.com");
        String viewerToken = registerAndGetToken("workspace-viewer@example.com");
        UUID organizationId = createOrganization(ownerToken, "Viewer Org", "viewer-org");
        UUID workspaceId = createWorkspace(ownerToken, organizationId, "Support", "support");
        String invitationToken = inviteAndGetToken(ownerToken, workspaceId, "workspace-viewer@example.com", WorkspaceRole.VIEWER);

        mockMvc.perform(post("/api/v1/workspaces/invitations/accept")
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcceptInvitationRequest(invitationToken))))
                .andExpect(status().isOk());

        UpdateWorkspaceRequest updateRequest = new UpdateWorkspaceRequest("Support Updated", "support-updated", null);
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "Password123!", "Test User");
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
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

    private String inviteAndGetToken(String token, UUID workspaceId, String email, WorkspaceRole role) throws Exception {
        InviteWorkspaceMemberRequest request = new InviteWorkspaceMemberRequest(email, role);
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/invitations", workspaceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private UUID findMemberIdByEmail(String membersResponse, String email) throws Exception {
        for (JsonNode member : objectMapper.readTree(membersResponse)) {
            if (email.equals(member.get("email").asText())) {
                return UUID.fromString(member.get("id").asText());
            }
        }
        throw new AssertionError("Member not found: " + email);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
