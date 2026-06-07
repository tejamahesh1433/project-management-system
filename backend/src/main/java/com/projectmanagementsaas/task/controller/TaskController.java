package com.projectmanagementsaas.task.controller;

import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.task.dto.AddTaskLabelRequest;
import com.projectmanagementsaas.task.dto.AssignTaskRequest;
import com.projectmanagementsaas.task.dto.ChangeTaskStatusRequest;
import com.projectmanagementsaas.task.dto.CreateLabelRequest;
import com.projectmanagementsaas.task.dto.CreateTaskCommentRequest;
import com.projectmanagementsaas.task.dto.CreateTaskRequest;
import com.projectmanagementsaas.task.dto.LabelResponse;
import com.projectmanagementsaas.task.dto.TaskCommentResponse;
import com.projectmanagementsaas.task.dto.TaskResponse;
import com.projectmanagementsaas.task.dto.UpdateTaskCommentRequest;
import com.projectmanagementsaas.task.dto.UpdateTaskRequest;
import com.projectmanagementsaas.task.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/tasks")
    ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.createTask(currentUser.id(), request));
    }

    @GetMapping("/tasks")
    ResponseEntity<List<TaskResponse>> listTasks(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam UUID projectId
    ) {
        return ResponseEntity.ok(taskService.listTasks(currentUser.id(), projectId));
    }

    @GetMapping("/tasks/{taskId}")
    ResponseEntity<TaskResponse> getTask(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId
    ) {
        return ResponseEntity.ok(taskService.getTask(currentUser.id(), taskId));
    }

    @PutMapping("/tasks/{taskId}")
    ResponseEntity<TaskResponse> updateTask(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(currentUser.id(), taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}")
    ResponseEntity<MessageResponse> deleteTask(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId
    ) {
        taskService.deleteTask(currentUser.id(), taskId);
        return ResponseEntity.ok(new MessageResponse("Task deleted"));
    }

    @PatchMapping("/tasks/{taskId}/assignee")
    ResponseEntity<TaskResponse> assignTask(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.assignTask(currentUser.id(), taskId, request));
    }

    @PatchMapping("/tasks/{taskId}/status")
    ResponseEntity<TaskResponse> changeStatus(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId,
            @Valid @RequestBody ChangeTaskStatusRequest request
    ) {
        return ResponseEntity.ok(taskService.changeStatus(currentUser.id(), taskId, request));
    }

    @PostMapping("/tasks/{taskId}/comments")
    ResponseEntity<TaskCommentResponse> createComment(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateTaskCommentRequest request
    ) {
        return ResponseEntity.ok(taskService.createComment(currentUser.id(), taskId, request));
    }

    @GetMapping("/tasks/{taskId}/comments")
    ResponseEntity<List<TaskCommentResponse>> listComments(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId
    ) {
        return ResponseEntity.ok(taskService.listComments(currentUser.id(), taskId));
    }

    @PutMapping("/tasks/{taskId}/comments/{commentId}")
    ResponseEntity<TaskCommentResponse> updateComment(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateTaskCommentRequest request
    ) {
        return ResponseEntity.ok(taskService.updateComment(currentUser.id(), taskId, commentId, request));
    }

    @PostMapping("/labels")
    ResponseEntity<LabelResponse> createLabel(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateLabelRequest request
    ) {
        return ResponseEntity.ok(taskService.createLabel(currentUser.id(), request));
    }

    @GetMapping("/labels")
    ResponseEntity<List<LabelResponse>> listLabels(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam UUID projectId
    ) {
        return ResponseEntity.ok(taskService.listLabels(currentUser.id(), projectId));
    }

    @PostMapping("/tasks/{taskId}/labels")
    ResponseEntity<TaskResponse> addLabel(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId,
            @Valid @RequestBody AddTaskLabelRequest request
    ) {
        return ResponseEntity.ok(taskService.addLabel(currentUser.id(), taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}/labels/{labelId}")
    ResponseEntity<TaskResponse> removeLabel(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID taskId,
            @PathVariable UUID labelId
    ) {
        return ResponseEntity.ok(taskService.removeLabel(currentUser.id(), taskId, labelId));
    }
}
