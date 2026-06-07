package com.projectmanagementsaas.report;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagementsaas.IntegrationTestBase;
import com.projectmanagementsaas.auth.dto.RegisterRequest;
import com.projectmanagementsaas.document.dto.CreateDocumentRequest;
import com.projectmanagementsaas.file.dto.CreateFileAssetRequest;
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.report.dto.GenerateReportRequest;
import com.projectmanagementsaas.report.entity.ReportType;
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
class ReportIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void reportsGenerateSnapshotsAndExport() throws Exception {
        UserSession owner = register("report-owner@example.com");
        ProjectSetup setup = createProjectWithWorkspace(owner.token(), "report-flow");
        UUID doneTaskId = createTask(owner.token(), setup.projectId(), "Done report task", 5);
        UUID openTaskId = createTask(owner.token(), setup.projectId(), "Open report task", 3);
        markDone(owner.token(), doneTaskId);
        UUID sprintId = createSprint(owner.token(), setup.projectId());
        addTaskToSprint(owner.token(), sprintId, doneTaskId);
        addTaskToSprint(owner.token(), sprintId, openTaskId);
        createDocument(owner.token(), setup.projectId());
        createFile(owner.token(), setup.projectId());

        String projectReportResponse = generate(owner.token(), new GenerateReportRequest(ReportType.PROJECT, null, setup.projectId(), null))
                .andExpect(jsonPath("$.type").value("PROJECT"))
                .andExpect(jsonPath("$.metrics.totalTasks").value(2))
                .andExpect(jsonPath("$.metrics.completedTasks").value(1))
                .andExpect(jsonPath("$.metrics.openTasks").value(1))
                .andExpect(jsonPath("$.metrics.members").value(1))
                .andExpect(jsonPath("$.metrics.completionPercentage").value(50.0))
                .andReturn().getResponse().getContentAsString();
        UUID projectReportId = UUID.fromString(objectMapper.readTree(projectReportResponse).get("id").asText());

        generate(owner.token(), new GenerateReportRequest(ReportType.SPRINT, null, null, sprintId))
                .andExpect(jsonPath("$.metrics.totalTasks").value(2))
                .andExpect(jsonPath("$.metrics.completedTasks").value(1))
                .andExpect(jsonPath("$.metrics.remainingTasks").value(1))
                .andExpect(jsonPath("$.metrics.storyPointsCompleted").value(5))
                .andExpect(jsonPath("$.metrics.storyPointsRemaining").value(3));

        generate(owner.token(), new GenerateReportRequest(ReportType.WORKSPACE, setup.workspaceId(), null, null))
                .andExpect(jsonPath("$.metrics.projects").value(1))
                .andExpect(jsonPath("$.metrics.tasks").value(2))
                .andExpect(jsonPath("$.metrics.documents").value(1))
                .andExpect(jsonPath("$.metrics.files").value(1))
                .andExpect(jsonPath("$.metrics.members").value(1));

        generate(owner.token(), new GenerateReportRequest(ReportType.TEAM, null, setup.projectId(), null))
                .andExpect(jsonPath("$.metrics.assignedTasks").value(0))
                .andExpect(jsonPath("$.metrics.completedTasks").value(0))
                .andExpect(jsonPath("$.metrics.openTasks").value(0))
                .andExpect(jsonPath("$.metrics.averageCompletionTimeHours").value(0.0));

        mockMvc.perform(get("/api/v1/reports")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        mockMvc.perform(get("/api/v1/reports/{id}", projectReportId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectReportId.toString()))
                .andExpect(jsonPath("$.metrics.totalTasks").value(2));

        mockMvc.perform(get("/api/v1/reports/{id}/export/json", projectReportId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTasks").value(2));

        mockMvc.perform(get("/api/v1/reports/{id}/export/csv", projectReportId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"totalTasks\",\"completedTasks\",\"openTasks\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"2\",\"1\",\"1\"")));
    }

    @Test
    void reportValidationRequiresCorrectTarget() throws Exception {
        UserSession owner = register("report-validation-owner@example.com");

        mockMvc.perform(post("/api/v1/reports/generate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenerateReportRequest(ReportType.PROJECT, null, null, null))))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions generate(String token, GenerateReportRequest request) throws Exception {
        return mockMvc.perform(post("/api/v1/reports/generate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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

    private UUID createTask(String token, UUID projectId, String title, int storyPoints) throws Exception {
        String response = mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTaskRequest(
                                projectId, null, title, null, TaskPriority.MEDIUM, TaskType.TASK, null, null, storyPoints))))
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
                                projectId,
                                "Report Sprint",
                                "Report sprint goal",
                                LocalDate.now().plusDays(1),
                                LocalDate.now().plusDays(14)))))
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
                        .content(objectMapper.writeValueAsString(new CreateDocumentRequest(projectId, null, "Report Notes", "Report content"))))
                .andExpect(status().isOk());
    }

    private void createFile(String token, UUID projectId) throws Exception {
        mockMvc.perform(post("/api/v1/files")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateFileAssetRequest(projectId, null, "report.csv", "text/csv", 128L))))
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
