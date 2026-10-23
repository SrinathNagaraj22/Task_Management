package com.taskflowManagement.project.controller;

import com.taskflowManagement.project.dto.AssignTaskRequest;
import com.taskflowManagement.project.dto.TaskRequest;
import com.taskflowManagement.project.dto.TaskStatusRequest;
import com.taskflowManagement.project.entity.Task;
import com.taskflowManagement.project.entity.User;
import com.taskflowManagement.project.repository.UserRepository;
import com.taskflowManagement.project.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService,
                          UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    private Long currentUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getId();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@Valid @RequestBody TaskRequest request,
                       Authentication authentication) {
        return taskService.create(request, currentUserId(authentication));
    }

    @GetMapping
    public List<Task> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        return taskService.getAll(currentUserId(authentication), status, search);
    }

    @GetMapping("/assigned-to-me")
    public List<Task> getAssignedToMe(Authentication authentication) {
        return taskService.getAssignedToMe(currentUserId(authentication));
    }

    @GetMapping("/{id}")
    public Task getById(@PathVariable Long id, Authentication authentication) {
        return taskService.getById(id, currentUserId(authentication));
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long id,
                       @Valid @RequestBody TaskRequest request,
                       Authentication authentication) {
        return taskService.update(id, request, currentUserId(authentication));
    }

    @PatchMapping("/{id}/status")
    public Task updateStatus(@PathVariable Long id,
                             @Valid @RequestBody TaskStatusRequest request,
                             Authentication authentication) {
        return taskService.updateStatus(id, request.status(), currentUserId(authentication));
    }

    @PatchMapping("/{id}/assign")
    public Task assign(@PathVariable Long id,
                       @Valid @RequestBody AssignTaskRequest request,
                       Authentication authentication) {
        return taskService.assign(id, request.assignedTo(), currentUserId(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        taskService.delete(id, currentUserId(authentication));
    }
}
