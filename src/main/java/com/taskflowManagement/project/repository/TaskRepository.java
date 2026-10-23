package com.taskflowManagement.project.repository;

import com.taskflowManagement.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    List<Task> findByUserIdAndStatus(Long userId, String status);

    List<Task> findByAssignedTo(Long assignedTo);

    @Query("""
           SELECT t FROM Task t
           WHERE t.userId = :userId
           AND (
               LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
           )
           """)
    List<Task> searchTasks(
            @Param("userId") Long userId,
            @Param("search") String search
    );

    @Query("""
           SELECT t FROM Task t
           WHERE t.userId = :userId
           AND t.status = :status
           AND (
               LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
           )
           """)
    List<Task> searchTasksByStatus(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("search") String search
    );
}
