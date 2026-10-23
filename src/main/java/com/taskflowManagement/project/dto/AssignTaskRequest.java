package com.taskflowManagement.project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignTaskRequest(
        @NotNull(message = "Assignee user ID is required")
        @Positive(message = "Assignee user ID must be positive")
        Long assignedTo
) {}
