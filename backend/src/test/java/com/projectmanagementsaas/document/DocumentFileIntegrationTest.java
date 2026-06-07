package com.projectmanagementsaas.document;

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
import com.projectmanagementsaas.document.dto.CreateDocumentRequest;
import com.projectmanagementsaas.document.dto.CreateFolderRequest;
import com.projectmanagementsaas.document.dto.UpdateDocumentRequest;
import com.projectmanagementsaas.document.entity.DocumentStatus;
import com.projectmanagementsaas.file.dto.CreateFileAssetRequest;
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
class DocumentFileIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void documentCrudVersioningAndRestoreWork() throws Exception {
        UserSession owner = register("doc-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "doc-flow");
        UUID folderId = createFolder(owner.token(), projectId);

        String createResponse = mockMvc.perform(post("/api/v1/documents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDocumentRequest(
                                projectId,
                                folderId,
                                "Project Brief",
                                "Version one"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVersion").value(1))
                .andReturn().getResponse().getContentAsString();
        UUID documentId = UUID.fromString(objectMapper.readTree(createResponse).get("id").asText());

        mockMvc.perform(put("/api/v1/documents/{documentId}", documentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateDocumentRequest(
                                folderId,
                                "Project Brief Updated",
                                "Version two",
                                DocumentStatus.PUBLISHED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.currentVersion").value(2));

        mockMvc.perform(get("/api/v1/documents/{documentId}/versions", documentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(post("/api/v1/documents/{documentId}/versions/{versionNumber}/restore", documentId, 1)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Project Brief"))
                .andExpect(jsonPath("$.content").value("Version one"))
                .andExpect(jsonPath("$.currentVersion").value(3));
    }

    @Test
    void fileMetadataFlowWorks() throws Exception {
        UserSession owner = register("file-owner@example.com");
        UUID projectId = createProjectWithWorkspace(owner.token(), "file-flow");
        UUID folderId = createFolder(owner.token(), projectId);

        String createResponse = mockMvc.perform(post("/api/v1/files")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateFileAssetRequest(
                                projectId,
                                folderId,
                                "brief.pdf",
                                "application/pdf",
                                12345L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("brief.pdf"))
                .andExpect(jsonPath("$.storagePath").exists())
                .andReturn().getResponse().getContentAsString();
        UUID fileId = UUID.fromString(objectMapper.readTree(createResponse).get("id").asText());

        mockMvc.perform(get("/api/v1/files")
                        .queryParam("projectId", projectId.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(fileId.toString()));

        mockMvc.perform(delete("/api/v1/files/{fileId}", fileId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File deleted"));
    }

    private UserSession register(String email) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "Password123!", "Test User");
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return new UserSession(email, UUID.fromString(json.get("user").get("id").asText()), json.get("accessToken").asText());
    }

    private UUID createProjectWithWorkspace(String token, String slugPrefix) throws Exception {
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
        return UUID.fromString(objectMapper.readTree(projectResponse).get("id").asText());
    }

    private UUID createFolder(String token, UUID projectId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/folders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateFolderRequest(projectId, null, "Docs"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record UserSession(String email, UUID userId, String token) {
    }
}
