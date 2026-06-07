package com.projectmanagementsaas.task.mapper;

import com.projectmanagementsaas.task.dto.LabelResponse;
import com.projectmanagementsaas.task.dto.TaskCommentResponse;
import com.projectmanagementsaas.task.dto.TaskResponse;
import com.projectmanagementsaas.task.entity.Label;
import com.projectmanagementsaas.task.entity.Task;
import com.projectmanagementsaas.task.entity.TaskComment;
import com.projectmanagementsaas.task.repository.TaskLabelRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    private final TaskLabelRepository taskLabelRepository;

    public TaskMapper(TaskLabelRepository taskLabelRepository) {
        this.taskLabelRepository = taskLabelRepository;
    }

    public TaskResponse toTaskResponse(Task task) {
        List<LabelResponse> labels = taskLabelRepository.findByTask_Id(task.getId()).stream()
                .map(taskLabel -> toLabelResponse(taskLabel.getLabel()))
                .toList();
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getParentTask() == null ? null : task.getParentTask().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getType(),
                task.getAssignee() == null ? null : task.getAssignee().getId(),
                task.getCreatedBy().getId(),
                task.getDueDate(),
                task.getStoryPoints(),
                labels,
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    public TaskCommentResponse toCommentResponse(TaskComment comment) {
        return new TaskCommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getEmail(),
                comment.getBody(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

    public LabelResponse toLabelResponse(Label label) {
        return new LabelResponse(
                label.getId(),
                label.getProject().getId(),
                label.getName(),
                label.getColor(),
                label.getCreatedAt());
    }
}
