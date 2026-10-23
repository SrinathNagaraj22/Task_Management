package com.taskflowManagement.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        LocalDateTime dueDate
) {}
