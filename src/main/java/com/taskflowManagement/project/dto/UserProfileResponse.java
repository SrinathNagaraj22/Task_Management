package com.taskflowManagement.project.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {
}