package com.taskflowManagement.project.service;

import com.taskflowManagement.project.entity.Comment;
import com.taskflowManagement.project.entity.Task;
import com.taskflowManagement.project.repository.CommentRepository;
import com.taskflowManagement.project.repository.TaskRepository;
import com.taskflowManagement.project.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;

    public CommentService(
            CommentRepository commentRepository,
            TaskRepository taskRepository,
            TeamMemberRepository teamMemberRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public Comment addComment(Long taskId, Long userId, String content) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        boolean canAccess = task.getUserId().equals(userId)
                || (task.getAssignedTo() != null && task.getAssignedTo().equals(userId));

        if (!canAccess) {
            throw new IllegalArgumentException("You cannot comment on this task");
        }

        return commentRepository.save(new Comment(taskId, userId, content));
    }

    @Transactional(readOnly = true)
    public List<Comment> getComments(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        boolean canAccess = task.getUserId().equals(userId)
                || (task.getAssignedTo() != null && task.getAssignedTo().equals(userId));

        if (!canAccess) {
            throw new IllegalArgumentException("You cannot view comments on this task");
        }

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }
}
