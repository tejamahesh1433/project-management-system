package com.projectmanagementsaas.ai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagementsaas.IntegrationTestBase;
import com.projectmanagementsaas.ai.dto.AiChatRequest;
import com.projectmanagementsaas.ai.dto.AiSearchRequest;
import com.projectmanagementsaas.ai.entity.AiModel;
import com.projectmanagementsaas.auth.dto.RegisterRequest;
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.task.dto.CreateTaskRequest;
import com.projectmanagementsaas.task.entity.TaskPriority;
import com.projectmanagementsaas.task.entity.TaskType;
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
class AiAssistantIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void chatSearchAndSummariesWorkWithLocalFallback() throws Exception {
        UserSession owner = register("ai-owner@example.com");
        ProjectSetup setup = createProjectWithWorkspace(owner.token(), "ai-flow");
        createTask(owner.token(), setup.projectId(), "Prepare AI assistant documentation");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AiChatRequest(
                                null,
                                setup.workspaceId(),
                                setup.projectId(),
                                AiModel.QWEN3,
                                "What should I know about the project?"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists())
                .andExpect(jsonPath("$.conversation.messages.length()").value(2));

        mockMvc.perform(post("/api/v1/ai/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AiSearchRequest(
                                setup.workspaceId(),
                                setup.projectId(),
                                "AI assistant documentation"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(org.hamcrest.Matchers.greaterThan(0)));

        mockMvc.perform(post("/api/v1/ai/summarize/project/{id}", setup.projectId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("PROJECT"))
                .andExpect(jsonPath("$.summary").exists());

        mockMvc.perform(post("/api/v1/ai/summarize/workspace/{id}", setup.workspaceId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("WORKSPACE"));
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

    private void createTask(String token, UUID projectId, String title) throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTaskRequest(
                                projectId, null, title, "Local AI assistant scope", TaskPriority.MEDIUM,
                                TaskType.TASK, null, null, 1))))
                .andExpect(status().isOk());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record UserSession(UUID userId, String token) {
    }

    private record ProjectSetup(UUID workspaceId, UUID projectId) {
    }
}
