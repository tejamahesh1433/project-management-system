package com.projectmanagementsaas.sprint.mapper;

import com.projectmanagementsaas.sprint.dto.SprintResponse;
import com.projectmanagementsaas.sprint.dto.SprintTaskResponse;
import com.projectmanagementsaas.sprint.entity.Sprint;
import com.projectmanagementsaas.sprint.entity.SprintTask;
import org.springframework.stereotype.Component;

@Component
public class SprintMapper {
    public SprintResponse toSprintResponse(Sprint sprint) {
        return new SprintResponse(
                sprint.getId(),
                sprint.getProject().getId(),
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getCreatedAt(),
                sprint.getUpdatedAt());
    }

    public SprintTaskResponse toSprintTaskResponse(SprintTask sprintTask) {
        return new SprintTaskResponse(
                sprintTask.getId(),
                sprintTask.getSprint().getId(),
                sprintTask.getTask().getId(),
                sprintTask.getTask().getTitle(),
                sprintTask.getTask().getStatus(),
                sprintTask.getTask().getStoryPoints(),
                sprintTask.getAddedAt());
    }
}
