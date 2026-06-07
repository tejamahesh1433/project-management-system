package com.projectmanagementsaas.notification;

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
import com.projectmanagementsaas.notification.dto.NotificationPreferenceItem;
import com.projectmanagementsaas.notification.dto.NotificationPreferencesRequest;
import com.projectmanagementsaas.notification.entity.NotificationType;
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.workspace.dto.CreateOrganizationRequest;
import com.projectmanagementsaas.workspace.dto.CreateWorkspaceRequest;
import com.projectmanagementsaas.workspace.dto.InviteWorkspaceMemberRequest;
import com.projectmanagementsaas.workspace.entity.WorkspaceRole;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class NotificationIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void projectEventsCreateNotificationsAndReadWorkflowWorks() throws Exception {
        UserSession owner = register("notification-owner@example.com");
        ProjectSetup setup = createProjectWithWorkspace(owner.token(), "notification-flow");

        String listResponse = mockMvc.perform(get("/api/v1/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("PROJECT_CREATED"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andReturn().getResponse().getContentAsString();
        UUID notificationId = UUID.fromString(objectMapper.readTree(listResponse).get(0).get("id").asText());

        mockMvc.perform(get("/api/v1/notifications/unread")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));

        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/notifications/{id}", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/notification-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(NotificationType.values().length));

        NotificationPreferencesRequest preferences = new NotificationPreferencesRequest(List.of(
                new NotificationPreferenceItem(NotificationType.PROJECT_CREATED, false)
        ));
        mockMvc.perform(put("/api/v1/notification-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferences)))
                .andExpect(status().isOk());

        createProject(owner.token(), setup.workspaceId(), "Muted Project", "muted-project");
        mockMvc.perform(get("/api/v1/notifications/unread")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void workspaceInvitationEventCreatesNotificationForExistingUser() throws Exception {
        UserSession owner = register("notification-invite-owner@example.com");
        UserSession invitee = register("notification-invitee@example.com");
        ProjectSetup setup = createProjectWithWorkspace(owner.token(), "notification-invite");

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/invitations", setup.workspaceId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new InviteWorkspaceMemberRequest(invitee.email(), WorkspaceRole.MEMBER))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(invitee.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("WORKSPACE_INVITATION"));
    }

    private UserSession register(String email) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "Password123!", "Test User");
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return new UserSession(email, UUID.fromString(json.get("user").get("id").asText()), json.get("accessToken").asText());
    }

    private ProjectSetup createProjectWithWorkspace(String token, String slugPrefix) throws Exception {
        String orgResponse = mockMvc.perform(post("/api/v1/organizations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateOrganizationRequest(slugPrefix + " Org", slugPrefix + "-org"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        UUID orgId = UUID.fromString(objectMapper.readTree(orgResponse).get("id").asText());

        String workspaceResponse = mockMvc.perform(post("/api/v1/workspaces")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWorkspaceRequest(orgId, slugPrefix + " Workspace", slugPrefix + "-workspace", null))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        UUID workspaceId = UUID.fromString(objectMapper.readTree(workspaceResponse).get("id").asText());
        UUID projectId = createProject(token, workspaceId, slugPrefix + " Project", slugPrefix + "-project");
        return new ProjectSetup(workspaceId, projectId);
    }

    private UUID createProject(String token, UUID workspaceId, String name, String slug) throws Exception {
        String projectResponse = mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProjectRequest(workspaceId, name, slug, null, null, null))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(projectResponse).get("id").asText());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record UserSession(String email, UUID userId, String token) {
    }

    private record ProjectSetup(UUID workspaceId, UUID projectId) {
    }
}
