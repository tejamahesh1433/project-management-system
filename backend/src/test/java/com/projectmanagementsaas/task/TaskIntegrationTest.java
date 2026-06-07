package com.projectmanagementsaas.task;

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
import com.projectmanagementsaas.project.dto.CreateProjectRequest;
import com.projectmanagementsaas.task.dto.AddTaskLabelRequest;
import com.projectmanagementsaas.task.dto.AssignTaskRequest;
import com.projectmanagementsaas.task.dto.ChangeTaskStatusRequest;
import com.projectmanagementsaas.task.dto.CreateLabelRequest;
import com.projectmanagementsaas.task.dto.CreateTaskCommentRequest;
import com.projectmanagementsaas.task.dto.CreateTaskRequest;
import com.projectmanagementsaas.task.dto.UpdateTaskCommentRequest;
import com.projectmanagementsaas.task.dto.UpdateTaskRequest;
import com.projectmanagementsaas.task.entity.TaskPriority;
import com.projectmanagementsaas.task.entity.TaskStatus;
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
class TaskIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void projectMemberCanCreateUpdateAssignChangeStatusAndDeleteTask() throws Exception {
        UserSession owner = register("task-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "task-flow");
        UUID taskId = createTask(owner.token(), projectId, null, "Implement API", TaskType.TASK);

        mockMvc.perform(get("/api/v1/tasks")
                        .queryParam("projectId", projectId.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(taskId.toString()));

        UpdateTaskRequest updateRequest = new UpdateTaskRequest(
                null,
                "Implement REST API",
                "Updated",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                TaskType.STORY,
                null,
                null);
        mockMvc.perform(put("/api/v1/tasks/{taskId}", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Implement REST API"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        mockMvc.perform(patch("/api/v1/tasks/{taskId}/assignee", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignTaskRequest(owner.userId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").value(owner.userId().toString()));

        mockMvc.perform(patch("/api/v1/tasks/{taskId}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeTaskStatusRequest(TaskStatus.IN_PROGRESS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(delete("/api/v1/tasks/{taskId}", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task deleted"));
    }

    @Test
    void supportsParentTaskHierarchy() throws Exception {
        UserSession owner = register("task-parent-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "task-parent");
        UUID epicId = createTask(owner.token(), projectId, null, "Epic", TaskType.EPIC);
        UUID childId = createTask(owner.token(), projectId, epicId, "Child task", TaskType.SUBTASK);

        mockMvc.perform(get("/api/v1/tasks/{taskId}", childId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentTaskId").value(epicId.toString()))
                .andExpect(jsonPath("$.type").value("SUBTASK"));
    }

    @Test
    void commentWorkflowWorks() throws Exception {
        UserSession owner = register("task-comment-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "task-comment");
        UUID taskId = createTask(owner.token(), projectId, null, "Commented task", TaskType.BUG);

        String response = mockMvc.perform(post("/api/v1/tasks/{taskId}/comments", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTaskCommentRequest("First comment"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("First comment"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID commentId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(put("/api/v1/tasks/{taskId}/comments/{commentId}", taskId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTaskCommentRequest("Updated comment"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Updated comment"));

        mockMvc.perform(get("/api/v1/tasks/{taskId}/comments", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].body").value("Updated comment"));
    }

    @Test
    void labelWorkflowWorks() throws Exception {
        UserSession owner = register("task-label-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "task-label");
        UUID taskId = createTask(owner.token(), projectId, null, "Label task", TaskType.TASK);

        String labelResponse = mockMvc.perform(post("/api/v1/labels")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLabelRequest(projectId, "backend", "#2563eb"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("backend"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID labelId = UUID.fromString(objectMapper.readTree(labelResponse).get("id").asText());

        mockMvc.perform(post("/api/v1/tasks/{taskId}/labels", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddTaskLabelRequest(labelId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels[0].id").value(labelId.toString()));

        mockMvc.perform(get("/api/v1/labels")
                        .queryParam("projectId", projectId.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(labelId.toString()));

        mockMvc.perform(delete("/api/v1/tasks/{taskId}/labels/{labelId}", taskId, labelId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels.length()").value(0));
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
        CreateOrganizationRequest organizationRequest = new CreateOrganizationRequest(slugPrefix + " Org", slugPrefix + "-org");
        String organizationResponse = mockMvc.perform(post("/api/v1/organizations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(organizationRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID organizationId = UUID.fromString(objectMapper.readTree(organizationResponse).get("id").asText());

        CreateWorkspaceRequest workspaceRequest = new CreateWorkspaceRequest(
                organizationId,
                slugPrefix + " Workspace",
                slugPrefix + "-workspace",
                null);
        String workspaceResponse = mockMvc.perform(post("/api/v1/workspaces")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workspaceRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID workspaceId = UUID.fromString(objectMapper.readTree(workspaceResponse).get("id").asText());

        CreateProjectRequest projectRequest = new CreateProjectRequest(
                workspaceId,
                slugPrefix + " Project",
                slugPrefix + "-project",
                null,
                null,
                null);
        String projectResponse = mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(projectResponse).get("id").asText());
    }

    private UUID createTask(String token, UUID projectId, UUID parentTaskId, String title, TaskType type) throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                projectId,
                parentTaskId,
                title,
                null,
                TaskPriority.MEDIUM,
                type,
                null,
                null);
        String response = mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
