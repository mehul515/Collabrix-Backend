package com.collabrix.collabrix.service;

import com.collabrix.collabrix.entity.Project;
import com.collabrix.collabrix.entity.ProjectInvite;
import com.collabrix.collabrix.entity.ProjectMember;
import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.repository.ProjectInviteRepository;
import com.collabrix.collabrix.repository.ProjectMemberRepository;
import com.collabrix.collabrix.repository.ProjectRepository;
import com.collabrix.collabrix.repository.UserRepository;
import com.collabrix.collabrix.request.ProjectInviteRequest;
import com.collabrix.collabrix.response.ProjectInviteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectInviteService {

    @Autowired
    private ProjectInviteRepository inviteRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private UserRepository userRepository;

    public ProjectInviteResponse sendInvite(Long inviterId, ProjectInviteRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Check if the inviter is the owner
        if (!project.getOwnerId().equals(inviterId)) {
            throw new RuntimeException("Only the project owner can send invites");
        }

        // Check if invited user exists
        User invitedUser = userRepository.findByEmail(request.getInvitedEmail());
        if (invitedUser == null) {
            throw new RuntimeException("Invited user does not exist");
        }

        // Check existing invite
        Optional<ProjectInvite> existingInviteOpt =
                inviteRepository.findByProjectIdAndInvitedEmail(request.getProjectId(), request.getInvitedEmail());

        if (existingInviteOpt.isPresent()) {
            ProjectInvite existingInvite = existingInviteOpt.get();
            String status = existingInvite.getStatus();

            if (!status.equalsIgnoreCase("declined")) {
                throw new RuntimeException("User already invited to this project");
            }

            // Delete old declined invite before resending
            inviteRepository.delete(existingInvite);
        }

        // Create new invite
        ProjectInvite invite = new ProjectInvite();
        invite.setProject(project);
        invite.setInvitedEmail(request.getInvitedEmail());
        invite.setInvitedBy(inviterId);
        invite.setRole(request.getRole());
        invite.setStatus("pending");

        ProjectInvite saved = inviteRepository.save(invite);
        return toResponse(saved);
    }


    public ProjectInviteResponse acceptInvite(UUID inviteId, Long userId, String userEmail) {
        ProjectInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!"pending".equalsIgnoreCase(invite.getStatus())) {
            throw new RuntimeException("Invite has already been responded to");
        }

        if (!invite.getInvitedEmail().equalsIgnoreCase(userEmail)) {
            throw new RuntimeException("You are not authorized to accept this invite");
        }

        // Prevent duplicate membership
        boolean isAlreadyMember = projectMemberRepository
                .existsByProjectIdAndUserId(invite.getProject().getId(), userId);
        if (isAlreadyMember) {
            throw new RuntimeException("You are already a member of this project");
        }

        // Update invite status
        invite.setStatus("accepted");
        invite.setUpdatedAt(Timestamp.from(Instant.now()));
        inviteRepository.save(invite);

        // Add to project members
        ProjectMember member = new ProjectMember();
        member.setProject(invite.getProject());
        member.setUserId(userId);
        member.setRole(invite.getRole());
        member.setJoinedAt(Timestamp.from(Instant.now()));
        member.setCreatedAt(Timestamp.from(Instant.now()));
        member.setUpdatedAt(Timestamp.from(Instant.now()));
        projectMemberRepository.save(member);

        return toResponse(invite);
    }


    public ProjectInviteResponse declineInvite(UUID inviteId, Long userId, String userEmail) {
        ProjectInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!"pending".equalsIgnoreCase(invite.getStatus())) {
            throw new RuntimeException("Invite has already been responded to");
        }

        if (!invite.getInvitedEmail().equalsIgnoreCase(userEmail)) {
            throw new RuntimeException("You are not authorized to decline this invite");
        }

        invite.setStatus("declined");
        invite.setUpdatedAt(Timestamp.from(Instant.now()));
        inviteRepository.save(invite);

        return toResponse(invite);
    }


    public List<ProjectInviteResponse> getUserInvites(String userEmail) {
        return inviteRepository.findByInvitedEmail(userEmail).stream()
                .filter(invite -> "pending".equalsIgnoreCase(invite.getStatus()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    private ProjectInviteResponse toResponse(ProjectInvite invite) {
        ProjectInviteResponse response = new ProjectInviteResponse();
        response.setId(invite.getId());
        response.setProjectId(invite.getProject().getId());
        response.setInvitedEmail(invite.getInvitedEmail());
        response.setRole(invite.getRole());
        response.setStatus(invite.getStatus());
        response.setInvitedBy(invite.getInvitedBy());
        response.setCreatedAt(invite.getCreatedAt());
        return response;
    }
}
