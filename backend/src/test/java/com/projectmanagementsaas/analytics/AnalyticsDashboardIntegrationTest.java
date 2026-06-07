package com.projectmanagementsaas.analytics;

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
import com.projectmanagementsaas.analytics.dto.CreateDashboardRequest;
import com.projectmanagementsaas.analytics.dto.CreateWidgetRequest;
import com.projectmanagementsaas.analytics.dto.UpdateDashboardRequest;
import com.projectmanagementsaas.analytics.dto.UpdateWidgetRequest;
import com.projectmanagementsaas.analytics.entity.WidgetType;
import com.projectmanagementsaas.auth.dto.RegisterRequest;
import com.projectmanagementsaas.document.dto.CreateDocumentRequest;
import com.projectmanagementsaas.file.dto.CreateFileAssetRequest;
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.sprint.dto.AddSprintTaskRequest;
import com.projectmanagementsaas.sprint.dto.CreateSprintRequest;
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
class AnalyticsDashboardIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dashboardWidgetCrudAndAnalyticsApisWork() throws Exception {
        UserSession owner = register("analytics-owner@example.com");
        ProjectSetup setup = createProjectWithWorkspace(owner.token(), "analytics-flow");
        UUID doneTaskId = createTask(owner, setup.projectId(), "Done analytics task", 5, LocalDate.now().minusDays(2));
        UUID openTaskId = createTask(owner, setup.projectId(), "Open analytics task", 3, LocalDate.now().minusDays(1));
        markDone(owner.token(), doneTaskId);
        UUID sprintId = createSprint(owner.token(), setup.projectId());
        addTaskToSprint(owner.token(), sprintId, doneTaskId);
        addTaskToSprint(owner.token(), sprintId, openTaskId);
        createDocument(owner.token(), setup.projectId());
        createFile(owner.token(), setup.projectId());

        String dashboardResponse = mockMvc.perform(post("/api/v1/dashboards")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDashboardRequest(
                                setup.workspaceId(), setup.projectId(), "Delivery Dashboard"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Delivery Dashboard"))
                .andExpect(jsonPath("$.widgets.length()").value(0))
                .andReturn().getResponse().getContentAsString();
        UUID dashboardId = UUID.fromString(objectMapper.readTree(dashboardResponse).get("id").asText());

        String widgetResponse = mockMvc.perform(post("/api/v1/dashboards/{dashboardId}/widgets", dashboardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWidgetRequest(
                                WidgetType.TASK_STATUS_CHART, "Task Status", 1, "{\"size\":\"wide\"}"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TASK_STATUS_CHART"))
                .andExpect(jsonPath("$.position").value(1))
                .andReturn().getResponse().getContentAsString();
        UUID widgetId = UUID.fromString(objectMapper.readTree(widgetResponse).get("id").asText());

        mockMvc.perform(put("/api/v1/dashboards/{dashboardId}", dashboardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateDashboardRequest("Updated Dashboard"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Dashboard"))
                .andExpect(jsonPath("$.widgets.length()").value(1));

        mockMvc.perform(put("/api/v1/dashboards/{dashboardId}/widgets/{widgetId}", dashboardId, widgetId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateWidgetRequest(
                                WidgetType.PROJECT_HEALTH, "Project Health", 2, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PROJECT_HEALTH"))
                .andExpect(jsonPath("$.position").value(2));

        mockMvc.perform(get("/api/v1/dashboards")
                        .queryParam("workspaceId", setup.workspaceId().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/v1/analytics/workspaces/{workspaceId}", setup.workspaceId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects").value(1))
                .andExpect(jsonPath("$.tasks").value(2))
                .andExpect(jsonPath("$.documents").value(1))
                .andExpect(jsonPath("$.files").value(1))
                .andExpect(jsonPath("$.members").value(1));

        mockMvc.perform(get("/api/v1/analytics/projects/{projectId}", setup.projectId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(2))
                .andExpect(jsonPath("$.statusBreakdown.DONE").value(1))
                .andExpect(jsonPath("$.statusBreakdown.TODO").value(1));

        mockMvc.perform(get("/api/v1/analytics/sprints/{sprintId}", sprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.velocity").value(5))
                .andExpect(jsonPath("$.completionPercentage").value(50.0))
                .andExpect(jsonPath("$.storyPointsCompleted").value(5))
                .andExpect(jsonPath("$.storyPointsRemaining").value(3));

        mockMvc.perform(get("/api/v1/analytics/teams/projects/{projectId}", setup.projectId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTasks").value(2))
                .andExpect(jsonPath("$.completedTasks").value(1))
                .andExpect(jsonPath("$.openTasks").value(1))
                .andExpect(jsonPath("$.overdueTasks").value(1));

        mockMvc.perform(delete("/api/v1/dashboards/{dashboardId}/widgets/{widgetId}", dashboardId, widgetId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/dashboards/{dashboardId}", dashboardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk());
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

        String projectResponse = mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProjectRequest(workspaceId, slugPrefix + " Project", slugPrefix + "-project", null, null, null))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        UUID projectId = UUID.fromString(objectMapper.readTree(projectResponse).get("id").asText());
        return new ProjectSetup(workspaceId, projectId);
    }

    private UUID createTask(UserSession owner, UUID projectId, String title, int storyPoints, LocalDate dueDate) throws Exception {
        String response = mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTaskRequest(
                                projectId, null, title, null, TaskPriority.MEDIUM, TaskType.TASK, owner.userId(), dueDate, storyPoints))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private void markDone(String token, UUID taskId) throws Exception {
        mockMvc.perform(patch("/api/v1/tasks/{taskId}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeTaskStatusRequest(TaskStatus.DONE))))
                .andExpect(status().isOk());
    }

    private UUID createSprint(String token, UUID projectId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/sprints")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSprintRequest(
                                projectId, "Analytics Sprint", "Metrics", LocalDate.now().plusDays(1), LocalDate.now().plusDays(14)))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private void addTaskToSprint(String token, UUID sprintId, UUID taskId) throws Exception {
        mockMvc.perform(post("/api/v1/sprints/{sprintId}/tasks", sprintId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddSprintTaskRequest(taskId))))
                .andExpect(status().isOk());
    }

    private void createDocument(String token, UUID projectId) throws Exception {
        mockMvc.perform(post("/api/v1/documents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDocumentRequest(projectId, null, "Analytics Notes", "Content"))))
                .andExpect(status().isOk());
    }

    private void createFile(String token, UUID projectId) throws Exception {
        mockMvc.perform(post("/api/v1/files")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateFileAssetRequest(projectId, null, "analytics.csv", "text/csv", 100L))))
                .andExpect(status().isOk());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record UserSession(String email, UUID userId, String token) {
    }

    private record ProjectSetup(UUID workspaceId, UUID projectId) {
    }
}
