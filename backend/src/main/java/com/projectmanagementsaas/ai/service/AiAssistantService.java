package com.projectmanagementsaas.ai.service;

import com.projectmanagementsaas.ai.dto.*;
import com.projectmanagementsaas.ai.entity.*;
import com.projectmanagementsaas.ai.mapper.AiMapper;
import com.projectmanagementsaas.ai.repository.AiConversationRepository;
import com.projectmanagementsaas.ai.repository.AiMessageRepository;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.service.ProjectAccessService;
import com.projectmanagementsaas.sprint.entity.Sprint;
import com.projectmanagementsaas.sprint.repository.SprintRepository;
import com.projectmanagementsaas.sprint.repository.SprintTaskRepository;
import com.projectmanagementsaas.task.entity.TaskStatus;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import com.projectmanagementsaas.workspace.service.WorkspaceAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiAssistantService {
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final ProjectAccessService projectAccessService;
    private final SprintRepository sprintRepository;
    private final SprintTaskRepository sprintTaskRepository;
    private final RagService ragService;
    private final OllamaClient ollamaClient;
    private final AiMapper mapper;

    public AiAssistantService(AiConversationRepository conversationRepository, AiMessageRepository messageRepository,
            UserRepository userRepository, WorkspaceAccessService workspaceAccessService,
            ProjectAccessService projectAccessService, SprintRepository sprintRepository,
            SprintTaskRepository sprintTaskRepository, RagService ragService, OllamaClient ollamaClient, AiMapper mapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.workspaceAccessService = workspaceAccessService;
        this.projectAccessService = projectAccessService;
        this.sprintRepository = sprintRepository;
        this.sprintTaskRepository = sprintTaskRepository;
        this.ragService = ragService;
        this.ollamaClient = ollamaClient;
        this.mapper = mapper;
    }

    @Transactional
    public AiChatResponse chat(UUID currentUserId, AiChatRequest request) {
        workspaceAccessService.requireMembership(request.workspaceId(), currentUserId);
        if (request.projectId() != null) {
            projectAccessService.requireProjectMember(request.projectId(), currentUserId);
        }
        ragService.indexWorkspace(request.workspaceId());
        AiConversation conversation = resolveConversation(currentUserId, request);
        saveMessage(conversation, AiMessageRole.USER, request.message());
        String context = ragService.fullContext(request.workspaceId(), request.projectId());
        String prompt = "Use only this project-management context. Do not create tasks or take actions.\nContext:\n"
                + context + "\nUser question:\n" + request.message();
        String answer = ollamaClient.chat(conversation.getModel(), prompt);
        saveMessage(conversation, AiMessageRole.ASSISTANT, answer);
        conversation.touch();
        conversationRepository.save(conversation);
        return new AiChatResponse(toResponse(conversation), answer);
    }

    @Transactional
    public AiSummaryResponse summarizeProject(UUID currentUserId, UUID projectId) {
        Project project = projectAccessService.requireProject(projectId);
        projectAccessService.requireProjectMember(projectId, currentUserId);
        ragService.indexWorkspace(project.getWorkspace().getId());
        String context = ragService.fullContext(project.getWorkspace().getId(), projectId);
        return new AiSummaryResponse("PROJECT", projectId, ollamaClient.chat(AiModel.QWEN3,
                "Summarize this project without recommendations or automation:\n" + context));
    }

    @Transactional
    public AiSummaryResponse summarizeSprint(UUID currentUserId, UUID sprintId) {
        Sprint sprint = sprintRepository.findByIdAndDeletedAtIsNull(sprintId).orElseThrow(() -> new NotFoundException("Sprint not found"));
        projectAccessService.requireProjectMember(sprint.getProject().getId(), currentUserId);
        List<com.projectmanagementsaas.sprint.entity.SprintTask> sprintTasks = sprintTaskRepository.findBySprint_IdOrderByAddedAtAsc(sprintId);
        long done = sprintTasks.stream().filter(item -> item.getTask().getStatus() == TaskStatus.DONE).count();
        int points = sprintTasks.stream().mapToInt(item -> item.getTask().getStoryPoints()).sum();
        String prompt = "Summarize sprint " + sprint.getName() + ". Tasks: " + sprintTasks.size()
                + ", done: " + done + ", story points: " + points + ". Do not recommend automated actions.";
        return new AiSummaryResponse("SPRINT", sprintId, ollamaClient.chat(AiModel.QWEN3, prompt));
    }

    @Transactional
    public AiSummaryResponse summarizeWorkspace(UUID currentUserId, UUID workspaceId) {
        workspaceAccessService.requireMembership(workspaceId, currentUserId);
        ragService.indexWorkspace(workspaceId);
        String context = ragService.fullContext(workspaceId, null);
        return new AiSummaryResponse("WORKSPACE", workspaceId, ollamaClient.chat(AiModel.QWEN3,
                "Summarize this workspace without recommendations or automation:\n" + context));
    }

    @Transactional
    public AiSearchResponse search(UUID currentUserId, AiSearchRequest request) {
        workspaceAccessService.requireMembership(request.workspaceId(), currentUserId);
        if (request.projectId() != null) {
            projectAccessService.requireProjectMember(request.projectId(), currentUserId);
        }
        ragService.indexWorkspace(request.workspaceId());
        return new AiSearchResponse(request.query(), ragService.search(request.workspaceId(), request.projectId(), request.query()));
    }

    @Transactional(readOnly = true)
    public List<AiConversationResponse> conversations(UUID currentUserId, UUID workspaceId) {
        workspaceAccessService.requireMembership(workspaceId, currentUserId);
        return conversationRepository.findByWorkspaceIdAndCreatedBy_IdOrderByUpdatedAtDesc(workspaceId, currentUserId)
                .stream().map(this::toResponse).toList();
    }

    private AiConversation resolveConversation(UUID currentUserId, AiChatRequest request) {
        if (request.conversationId() != null) {
            return conversationRepository.findByIdAndCreatedBy_Id(request.conversationId(), currentUserId)
                    .orElseThrow(() -> new NotFoundException("Conversation not found"));
        }
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new NotFoundException("User not found"));
        AiConversation conversation = new AiConversation();
        conversation.setWorkspaceId(request.workspaceId());
        conversation.setProjectId(request.projectId());
        conversation.setScope(request.projectId() == null ? AiConversationScope.WORKSPACE : AiConversationScope.PROJECT);
        conversation.setTitle(request.message().length() > 80 ? request.message().substring(0, 80) : request.message());
        conversation.setModel(request.model() == null ? AiModel.QWEN3 : request.model());
        conversation.setCreatedBy(user);
        return conversationRepository.save(conversation);
    }

    private void saveMessage(AiConversation conversation, AiMessageRole role, String content) {
        AiMessage message = new AiMessage();
        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);
        messageRepository.save(message);
    }

    private AiConversationResponse toResponse(AiConversation conversation) {
        return mapper.toConversationResponse(conversation, messageRepository.findByConversation_IdOrderByCreatedAtAsc(conversation.getId()));
    }
}
