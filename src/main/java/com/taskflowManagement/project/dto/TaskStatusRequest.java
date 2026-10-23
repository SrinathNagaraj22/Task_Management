package com.taskflowManagement.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TaskStatusRequest(
        @NotBlank
        @Pattern(regexp = "TODO|IN_PROGRESS|COMPLETED",
                message = "Status must be TODO, IN_PROGRESS, or COMPLETED")
        String status
) {}
