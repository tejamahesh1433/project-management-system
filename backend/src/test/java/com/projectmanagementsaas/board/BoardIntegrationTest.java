package com.projectmanagementsaas.board;

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
import com.projectmanagementsaas.board.dto.CreateBoardColumnRequest;
import com.projectmanagementsaas.board.dto.CreateBoardRequest;
import com.projectmanagementsaas.board.dto.MoveTaskRequest;
import com.projectmanagementsaas.board.dto.UpdateBoardColumnRequest;
import com.projectmanagementsaas.board.dto.UpdateBoardRequest;
import com.projectmanagementsaas.board.entity.BoardTemplate;
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
class BoardIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void supportsMultipleBoardsPerProjectAndBoardCrud() throws Exception {
        UserSession owner = register("board-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "board-crud");
        UUID kanbanBoardId = createBoard(owner.token(), projectId, "Kanban Board", BoardTemplate.KANBAN);
        UUID scrumBoardId = createBoard(owner.token(), projectId, "Scrum Board", BoardTemplate.SCRUM);

        mockMvc.perform(get("/api/v1/boards")
                        .queryParam("projectId", projectId.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/v1/boards/{boardId}", kanbanBoardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns.length()").value(3));

        mockMvc.perform(get("/api/v1/boards/{boardId}", scrumBoardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns.length()").value(4));

        mockMvc.perform(put("/api/v1/boards/{boardId}", kanbanBoardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateBoardRequest("Updated Board"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Board"));

        mockMvc.perform(delete("/api/v1/boards/{boardId}", scrumBoardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Board deleted"));
    }

    @Test
    void supportsColumnCrudAndTaskMovementOrdering() throws Exception {
        UserSession owner = register("board-move-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "board-move");
        UUID boardId = createBoard(owner.token(), projectId, "Delivery", BoardTemplate.KANBAN);
        JsonNode board = getBoard(owner.token(), boardId);
        UUID todoColumnId = UUID.fromString(board.get("columns").get(0).get("id").asText());
        UUID doneColumnId = UUID.fromString(board.get("columns").get(2).get("id").asText());
        UUID firstTaskId = createTask(owner.token(), projectId, "First task");
        UUID secondTaskId = createTask(owner.token(), projectId, "Second task");

        mockMvc.perform(patch("/api/v1/boards/{boardId}/tasks/move", boardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MoveTaskRequest(firstTaskId, todoColumnId, 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[0].tasks[0].taskId").value(firstTaskId.toString()))
                .andExpect(jsonPath("$.columns[0].tasks[0].position").value(0));

        mockMvc.perform(patch("/api/v1/boards/{boardId}/tasks/move", boardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MoveTaskRequest(secondTaskId, todoColumnId, 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[0].tasks[0].position").value(0))
                .andExpect(jsonPath("$.columns[0].tasks[1].position").value(1));

        mockMvc.perform(patch("/api/v1/boards/{boardId}/tasks/move", boardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MoveTaskRequest(firstTaskId, doneColumnId, 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[2].tasks[0].taskId").value(firstTaskId.toString()))
                .andExpect(jsonPath("$.columns[2].tasks[0].position").value(0));

        String createdColumn = mockMvc.perform(post("/api/v1/boards/{boardId}/columns", boardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateBoardColumnRequest("Review", 1))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID columnId = UUID.fromString(objectMapper.readTree(createdColumn).get("id").asText());

        mockMvc.perform(put("/api/v1/boards/{boardId}/columns/{columnId}", boardId, columnId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateBoardColumnRequest("Code Review", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Code Review"));

        mockMvc.perform(delete("/api/v1/boards/{boardId}/columns/{columnId}", boardId, columnId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
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

    private UUID createBoard(String token, UUID projectId, String name, BoardTemplate template) throws Exception {
        String response = mockMvc.perform(post("/api/v1/boards")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateBoardRequest(projectId, name, template))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID createTask(String token, UUID projectId, String title) throws Exception {
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
                                null))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private JsonNode getBoard(String token, UUID boardId) throws Exception {
        String response = mockMvc.perform(get("/api/v1/boards/{boardId}", boardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record UserSession(String email, UUID userId, String token) {
    }
}
