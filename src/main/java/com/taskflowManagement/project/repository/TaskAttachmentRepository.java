package com.taskflowManagement.project.repository;

import com.taskflowManagement.project.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskAttachmentRepository
        extends JpaRepository<TaskAttachment, Long> {

    List<TaskAttachment> findByTaskIdOrderByUploadedAtDesc(Long taskId);

    Optional<TaskAttachment> findByIdAndTaskId(
            Long id, Long taskId);
}