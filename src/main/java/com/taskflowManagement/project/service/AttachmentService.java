package com.taskflowManagement.project.service;

import com.taskflowManagement.project.dto.AttachmentResponse;
import com.taskflowManagement.project.entity.Task;
import com.taskflowManagement.project.entity.TaskAttachment;
import com.taskflowManagement.project.repository.TaskAttachmentRepository;
import com.taskflowManagement.project.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final Path uploadDirectory;

    public AttachmentService(
            TaskAttachmentRepository attachmentRepository,
            TaskRepository taskRepository,
            @Value("${app.upload.dir:uploads}") String uploadDirectory) {

        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;

        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not initialize attachment storage", ex);
        }
    }

    @Transactional
    public AttachmentResponse upload(
            Long taskId, Long userId, MultipartFile file) {

        Task task = getAccessibleTask(taskId, userId);

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please select a file");
        }

        if (file.getSize() > 10L * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "File size must not exceed 10 MB");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid filename");
        }

        // Keep only the filename, not any client-supplied path.
        originalFilename = Paths.get(
                        originalFilename.replace('\\', '/'))
                .getFileName()
                .toString();

        if (originalFilename.isBlank()
                || originalFilename.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid filename");
        }

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        if (contentType.length() > 100) {
            contentType = "application/octet-stream";
        }

        String storedFilename = UUID.randomUUID().toString();
        Path destination = uploadDirectory.resolve(storedFilename)
                .normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file path");
        }

        boolean fileWritten = false;

        try {
            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);

            fileWritten = true;

            TaskAttachment attachment = new TaskAttachment();
            attachment.setTaskId(task.getId());
            attachment.setUploadedBy(userId);
            attachment.setOriginalFilename(originalFilename);
            attachment.setStoredFilename(storedFilename);
            attachment.setContentType(contentType);
            attachment.setFileSize(file.getSize());

            return AttachmentResponse.from(
                    attachmentRepository.save(attachment));

        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save attachment");
        } catch (RuntimeException ex) {
            if (fileWritten) {
                try {
                    Files.deleteIfExists(destination);
                } catch (IOException ignored) {
                    // Preserve the original exception.
                }
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> list(
            Long taskId, Long userId) {

        getAccessibleTask(taskId, userId);

        return attachmentRepository
                .findByTaskIdOrderByUploadedAtDesc(taskId)
                .stream()
                .map(AttachmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadedAttachment download(
            Long taskId, Long attachmentId, Long userId) {

        getAccessibleTask(taskId, userId);

        TaskAttachment attachment = attachmentRepository
                .findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Attachment not found"));

        Path filePath = uploadDirectory
                .resolve(attachment.getStoredFilename())
                .normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file path");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Attachment file not found");
            }

            return new DownloadedAttachment(attachment, resource);

        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not read attachment");
        }
    }

    @Transactional
    public void delete(
            Long taskId, Long attachmentId, Long userId) {

        getAccessibleTask(taskId, userId);

        TaskAttachment attachment = attachmentRepository
                .findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Attachment not found"));

        // Only the uploader or task owner may delete the attachment.
        Task task = taskRepository.findById(taskId).orElseThrow();

        if (!attachment.getUploadedBy().equals(userId)
                && !task.getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the uploader or task owner can delete this attachment");
        }

        Path filePath = uploadDirectory
                .resolve(attachment.getStoredFilename())
                .normalize();

        try {
            Files.deleteIfExists(filePath);
            attachmentRepository.delete(attachment);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not delete attachment");
        }
    }

    private Task getAccessibleTask(Long taskId, Long userId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"));

        boolean isOwner = task.getUserId().equals(userId);
        boolean isAssignee = task.getAssignedTo() != null
                && task.getAssignedTo().equals(userId);

        if (!isOwner && !isAssignee) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access attachments for this task");
        }

        return task;
    }

    public record DownloadedAttachment(
            TaskAttachment attachment,
            Resource resource) {
    }
}