package com.taskflowManagement.project.service;

import com.taskflowManagement.project.dto.TaskRequest;
import com.taskflowManagement.project.entity.Task;
import com.taskflowManagement.project.repository.TaskRepository;
import com.taskflowManagement.project.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Task create(TaskRequest request, Long userId) {
        Task task = new Task(
                request.title(),
                request.description(),
                request.dueDate(),
                userId
        );
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getAll(Long userId, String status, String search) {
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasSearch = search != null && !search.isBlank();

        if (hasStatus && hasSearch) {
            return taskRepository.searchTasksByStatus(userId, status, search);
        }
        if (hasStatus) {
            return taskRepository.findByUserIdAndStatus(userId, status);
        }
        if (hasSearch) {
            return taskRepository.searchTasks(userId, search);
        }
        return taskRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Task getById(Long id, Long userId) {
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    public Task update(Long id, TaskRequest request, Long userId) {
        Task task = getById(id, userId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        return taskRepository.save(task);
    }

    public Task updateStatus(Long id, String status, Long userId) {
        Task task = getById(id, userId);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public Task assign(Long id, Long assignedTo, Long ownerId) {
        Task task = getById(id, ownerId);

        if (!userRepository.existsById(assignedTo)) {
            throw new IllegalArgumentException("Assignee user not found");
        }

        task.setAssignedTo(assignedTo);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getAssignedToMe(Long userId) {
        return taskRepository.findByAssignedTo(userId);
    }

    public void delete(Long id, Long userId) {
        Task task = getById(id, userId);
        taskRepository.delete(task);
    }
}
