package com.projectmanagementsaas.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagementsaas.IntegrationTestBase;
import com.projectmanagementsaas.auth.dto.RegisterRequest;
import com.projectmanagementsaas.integration.dto.CreateIntegrationRequest;
import com.projectmanagementsaas.integration.entity.IntegrationType;
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.workspace.dto.CreateOrganizationRequest;
import com.projectmanagementsaas.workspace.dto.CreateWorkspaceRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class IntegrationIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void integrationCrudConnectionTestAndWebhooksWork() throws Exception {
        UserSession owner = register("integration-owner@example.com");
        ProjectSetup setup = createProjectWithWorkspace(owner.token(), "integration-flow");

        String createResponse = mockMvc.perform(post("/api/v1/integrations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIntegrationRequest(
                                setup.workspaceId(),
                                setup.projectId(),
                                IntegrationType.GITHUB,
                                "GitHub Repo",
                                "https://github.example.local/webhook",
                                "https://github.example.local/acme/project",
                                "acme/project",
                                "{\"defaultBranch\":\"main\"}"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("GITHUB"))
                .andExpect(jsonPath("$.connections.length()").value(1))
                .andExpect(jsonPath("$.webhooks.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        UUID integrationId = UUID.fromString(objectMapper.readTree(createResponse).get("id").asText());

        mockMvc.perform(get("/api/v1/integrations")
                        .queryParam("workspaceId", setup.workspaceId().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(integrationId.toString()));

        mockMvc.perform(post("/api/v1/integrations/{id}/test", integrationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/v1/webhooks/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repository\":{\"full_name\":\"acme/project\"},\"action\":\"push\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("github"));

        mockMvc.perform(get("/api/v1/integrations/{id}", integrationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connections[0].status").value("CONNECTED"))
                .andExpect(jsonPath("$.webhooks[0].lastReceivedAt").exists());

        mockMvc.perform(delete("/api/v1/integrations/{id}", integrationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk());
    }

    private UserSession register(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, "Password123!", "Test User"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return new UserSession(UUID.fromString(json.get("user").get("id").asText()), json.get("accessToken").asText());
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

        String projectResponse = mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProjectRequest(workspaceId, slugPrefix + " Project", slugPrefix + "-project", null, null, null))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        UUID projectId = UUID.fromString(objectMapper.readTree(projectResponse).get("id").asText());
        return new ProjectSetup(workspaceId, projectId);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record UserSession(UUID userId, String token) {
    }

    private record ProjectSetup(UUID workspaceId, UUID projectId) {
    }
}
