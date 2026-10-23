package com.taskflowManagement.project.repository;

import com.taskflowManagement.project.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByCreatedBy(Long userId);

    Optional<Team> findByIdAndCreatedBy(Long id, Long userId);
}
