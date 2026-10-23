package com.taskflowManagement.project.service;

import com.taskflowManagement.project.dto.TeamRequest;
import com.taskflowManagement.project.entity.Team;
import com.taskflowManagement.project.entity.TeamMember;
import com.taskflowManagement.project.repository.TeamMemberRepository;
import com.taskflowManagement.project.repository.TeamRepository;
import com.taskflowManagement.project.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository memberRepository;
    private final UserRepository userRepository;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository memberRepository,
            UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public Team create(TeamRequest request, Long userId) {
        Team team = new Team(
                request.name(),
                request.description(),
                userId
        );

        team = teamRepository.save(team);

        // Automatically add the creator as the team admin.
        memberRepository.save(new TeamMember(team.getId(), userId, "ADMIN"));

        return team;
    }

    @Transactional(readOnly = true)
    public List<Team> getMyTeams(Long userId) {
        return memberRepository.findByUserId(userId)
                .stream()
                .map(member -> teamRepository.findById(member.getTeamId()).orElse(null))
                .filter(team -> team != null)
                .distinct()
                .toList();
    }

    public TeamMember addMember(Long teamId, Long memberUserId, Long currentUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        if (!team.getCreatedBy().equals(currentUserId)) {
            throw new IllegalArgumentException("Only the team creator can add members");
        }

        if (!userRepository.existsById(memberUserId)) {
            throw new IllegalArgumentException("User not found");
        }

        if (memberRepository.existsByTeamIdAndUserId(teamId, memberUserId)) {
            throw new IllegalArgumentException("User is already a team member");
        }

        return memberRepository.save(
                new TeamMember(teamId, memberUserId, "MEMBER")
        );
    }

    @Transactional(readOnly = true)
    public List<TeamMember> getMembers(Long teamId, Long currentUserId) {
        if (!memberRepository.existsByTeamIdAndUserId(teamId, currentUserId)) {
            throw new IllegalArgumentException("You are not a member of this team");
        }

        return memberRepository.findByTeamId(teamId);
    }
}