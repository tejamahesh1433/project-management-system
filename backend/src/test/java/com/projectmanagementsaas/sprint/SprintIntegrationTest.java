package com.projectmanagementsaas.sprint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagementsaas.IntegrationTestBase;
import com.projectmanagementsaas.auth.dto.RegisterRequest;
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.sprint.dto.AddSprintTaskRequest;
import com.projectmanagementsaas.sprint.dto.CreateSprintRequest;
import com.projectmanagementsaas.sprint.dto.UpdateSprintRequest;
import com.projectmanagementsaas.task.dto.ChangeTaskStatusRequest;
import com.projectmanagementsaas.task.dto.CreateTaskRequest;
import com.projectmanagementsaas.task.entity.TaskPriority;
import com.projectmanagementsaas.task.entity.TaskStatus;
import com.projectmanagementsaas.task.entity.TaskType;
import com.projectmanagementsaas.workspace.dto.CreateOrganizationRequest;
import com.projectmanagementsaas.workspace.dto.CreateWorkspaceRequest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class SprintIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sprintLifecycleWorksAndOnlyOneActiveSprintIsAllowed() throws Exception {
        UserSession owner = register("sprint-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "sprint-life");
        UUID firstSprintId = createSprint(owner.token(), projectId, "Sprint 1");
        UUID secondSprintId = createSprint(owner.token(), projectId, "Sprint 2");

        mockMvc.perform(put("/api/v1/sprints/{sprintId}", firstSprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateSprintRequest(
                                "Sprint One",
                                "Updated goal",
                                LocalDate.now().plusDays(1),
                                LocalDate.now().plusDays(14)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sprint One"));

        mockMvc.perform(post("/api/v1/sprints/{sprintId}/start", firstSprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/sprints/{sprintId}/start", secondSprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/sprints/{sprintId}/complete", firstSprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(post("/api/v1/sprints/{sprintId}/cancel", secondSprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void sprintTaskAssignmentAndMetricsWork() throws Exception {
        UserSession owner = register("sprint-metrics-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "sprint-metrics");
        UUID sprintId = createSprint(owner.token(), projectId, "Metrics Sprint");
        UUID firstTaskId = createTask(owner.token(), projectId, "Done task", 5);
        UUID secondTaskId = createTask(owner.token(), projectId, "Remaining task", 3);

        addTaskToSprint(owner.token(), sprintId, firstTaskId)
                .andExpect(jsonPath("$.storyPoints").value(5));
        addTaskToSprint(owner.token(), sprintId, secondTaskId)
                .andExpect(jsonPath("$.storyPoints").value(3));

        mockMvc.perform(post("/api/v1/sprints/{sprintId}/tasks", sprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddSprintTaskRequest(firstTaskId))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/sprints/{sprintId}/start", sprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/tasks/{taskId}/status", firstTaskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeTaskStatusRequest(TaskStatus.DONE))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/sprints/{sprintId}/metrics", sprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(2))
                .andExpect(jsonPath("$.completedTasks").value(1))
                .andExpect(jsonPath("$.remainingTasks").value(1))
                .andExpect(jsonPath("$.completionPercentage").value(50.0))
                .andExpect(jsonPath("$.storyPointsCompleted").value(5))
                .andExpect(jsonPath("$.storyPointsRemaining").value(3));

        mockMvc.perform(delete("/api/v1/sprints/{sprintId}/tasks/{taskId}", sprintId, secondTaskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/sprints/{sprintId}/tasks", sprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private org.springframework.test.web.servlet.ResultActions addTaskToSprint(String token, UUID sprintId, UUID taskId) throws Exception {
        return mockMvc.perform(post("/api/v1/sprints/{sprintId}/tasks", sprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddSprintTaskRequest(taskId))))
                .andExpect(status().isOk());
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
        return new UserSession(email, UUID.fromString(json.get("user").get("id").asText()), json.get("accessToken").asText());
    }

    private UUID createProjectWithWorkspace(String token, String slugPrefix) throws Exception {
        String organizationResponse = mockMvc.perform(post("/api/v1/organizations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateOrganizationRequest(slugPrefix + " Org", slugPrefix + "-org"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID organizationId = UUID.fromString(objectMapper.readTree(organizationResponse).get("id").asText());

        String workspaceResponse = mockMvc.perform(post("/api/v1/workspaces")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWorkspaceRequest(
                                organizationId,
                                slugPrefix + " Workspace",
                                slugPrefix + "-workspace",
                                null))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID workspaceId = UUID.fromString(objectMapper.readTree(workspaceResponse).get("id").asText());

        String projectResponse = mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProjectRequest(
                                workspaceId,
                                slugPrefix + " Project",
                                slugPrefix + "-project",
                                null,
                                null,
                                null))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(projectResponse).get("id").asText());
    }

    private UUID createSprint(String token, UUID projectId, String name) throws Exception {
        String response = mockMvc.perform(post("/api/v1/sprints")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSprintRequest(
                                projectId,
                                name,
                                "Goal",
                                LocalDate.now().plusDays(1),
                                LocalDate.now().plusDays(14)))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID createTask(String token, UUID projectId, String title, int storyPoints) throws Exception {
        String response = mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTaskRequest(
                                projectId,
                                null,
                                title,
                                null,
                                TaskPriority.MEDIUM,
                                TaskType.TASK,
                                null,
                                null,
                                storyPoints))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record UserSession(String email, UUID userId, String token) {
    }
}
