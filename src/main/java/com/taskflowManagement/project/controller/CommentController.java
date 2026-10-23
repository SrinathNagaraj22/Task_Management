package com.taskflowManagement.project.controller;

import com.taskflowManagement.project.dto.CommentRequest;
import com.taskflowManagement.project.entity.Comment;
import com.taskflowManagement.project.repository.UserRepository;
import com.taskflowManagement.project.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    public CommentController(
            CommentService commentService,
            UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    private Long currentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Comment addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        return commentService.addComment(
                taskId, currentUserId(authentication), request.content());
    }

    @GetMapping
    public List<Comment> getComments(
            @PathVariable Long taskId,
            Authentication authentication) {
        return commentService.getComments(taskId, currentUserId(authentication));
    }
}