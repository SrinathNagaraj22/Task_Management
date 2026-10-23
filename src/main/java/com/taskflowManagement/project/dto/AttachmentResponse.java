package com.taskflowManagement.project.dto;

import com.taskflowManagement.project.entity.TaskAttachment;
import java.time.LocalDateTime;

public record AttachmentResponse(
        Long id,
        Long taskId,
        Long uploadedBy,
        String originalFilename,
        String contentType,
        Long fileSize,
        LocalDateTime uploadedAt
) {
    public static AttachmentResponse from(TaskAttachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getTaskId(),
                attachment.getUploadedBy(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUploadedAt()
        );
    }
}