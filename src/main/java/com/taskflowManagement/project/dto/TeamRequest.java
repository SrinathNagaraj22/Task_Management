package com.taskflowManagement.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamRequest(
        @NotBlank(message = "Team name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
) {}
