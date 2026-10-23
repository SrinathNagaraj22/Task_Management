package com.taskflowManagement.project.controller;

import com.taskflowManagement.project.dto.AttachmentResponse;
import com.taskflowManagement.project.entity.User;
import com.taskflowManagement.project.service.AttachmentService;
import com.taskflowManagement.project.service.AttachmentService.DownloadedAttachment;
import com.taskflowManagement.project.repository.UserRepository;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final UserRepository userRepository;

    public AttachmentController(
            AttachmentService attachmentService,
            UserRepository userRepository) {
        this.attachmentService = attachmentService;
        this.userRepository = userRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        return ResponseEntity.status(201).body(
                attachmentService.upload(
                        taskId, currentUserId(authentication), file));
    }

    @GetMapping
    public ResponseEntity<List<AttachmentResponse>> list(
            @PathVariable Long taskId,
            Authentication authentication) {

        return ResponseEntity.ok(
                attachmentService.list(
                        taskId, currentUserId(authentication)));
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<Resource> download(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId,
            Authentication authentication) {

        DownloadedAttachment downloaded =
                attachmentService.download(
                        taskId, attachmentId,
                        currentUserId(authentication));

        String contentType = downloaded.attachment().getContentType();

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        String disposition = ContentDisposition.attachment()
                .filename(
                        downloaded.attachment().getOriginalFilename(),
                        StandardCharsets.UTF_8)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(downloaded.attachment().getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(downloaded.resource());
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId,
            Authentication authentication) {

        attachmentService.delete(
                taskId, attachmentId,
                currentUserId(authentication));

        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return user.getId();
    }
}