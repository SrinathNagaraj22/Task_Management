package com.taskflowManagement.project.controller;

import com.taskflowManagement.project.dto.AddTeamMemberRequest;
import com.taskflowManagement.project.dto.TeamRequest;
import com.taskflowManagement.project.entity.Team;
import com.taskflowManagement.project.entity.TeamMember;
import com.taskflowManagement.project.entity.User;
import com.taskflowManagement.project.repository.UserRepository;
import com.taskflowManagement.project.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final UserRepository userRepository;

    public TeamController(TeamService teamService, UserRepository userRepository) {
        this.teamService = teamService;
        this.userRepository = userRepository;
    }

    private Long currentUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getId();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Team create(@Valid @RequestBody TeamRequest request,
                       Authentication authentication) {
        return teamService.create(request, currentUserId(authentication));
    }

    @GetMapping
    public List<Team> getMyTeams(Authentication authentication) {
        return teamService.getMyTeams(currentUserId(authentication));
    }

    @PostMapping("/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMember addMember(
            @PathVariable Long teamId,
            @Valid @RequestBody AddTeamMemberRequest request,
            Authentication authentication) {
        return teamService.addMember(
                teamId, request.userId(), currentUserId(authentication));
    }

    @GetMapping("/{teamId}/members")
    public List<TeamMember> getMembers(
            @PathVariable Long teamId,
            Authentication authentication) {
        return teamService.getMembers(teamId, currentUserId(authentication));
    }
}