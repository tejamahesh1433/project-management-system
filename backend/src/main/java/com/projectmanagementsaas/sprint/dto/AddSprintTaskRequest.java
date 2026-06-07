package com.projectmanagementsaas.sprint.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddSprintTaskRequest(@NotNull UUID taskId) {
}
