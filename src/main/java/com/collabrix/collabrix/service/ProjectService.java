package com.collabrix.collabrix.service;

import com.collabrix.collabrix.entity.Project;
import com.collabrix.collabrix.entity.ProjectMember;
import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.repository.ProjectInviteRepository;
import com.collabrix.collabrix.repository.ProjectMemberRepository;
import com.collabrix.collabrix.repository.ProjectRepository;
import com.collabrix.collabrix.repository.UserRepository;
import com.collabrix.collabrix.request.ProjectRequest;
import com.collabrix.collabrix.response.ProjectResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectInviteRepository projectInviteRepository;

    public ProjectResponse createProject(ProjectRequest request, Long ownerId) {

        // Check if user exists
        if (!userRepository.existsById(ownerId)) {
            throw new IllegalArgumentException("User with this ID does not exist");
        }

        if (projectRepository.existsByNameAndOwnerId(request.getName(), ownerId)) {
            throw new IllegalArgumentException("Project with the same name already exists for this owner.");
        }

        if (request.getBudget() != null && request.getBudget().doubleValue() < 0) {
            throw new IllegalArgumentException("Project budget must be non-negative.");
        }

        if (request.getStartDate() != null && request.getDueDate() != null &&
                request.getStartDate().isAfter(request.getDueDate())) {
            throw new IllegalArgumentException("Start date cannot be after due date.");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setPriority(request.getPriority());
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());
        project.setBudget(request.getBudget());
        project.setTags(request.getTags());
        project.setOwnerId(ownerId);



        Project saved = projectRepository.save(project);

        if (!projectMemberRepository.existsByProjectIdAndUserId(saved.getId(), saved.getOwnerId())) {
            ProjectMember member = new ProjectMember();
            member.setProject(saved);
            member.setUserId(saved.getOwnerId());
            member.setRole("owner");
            member.setJoinedAt(Timestamp.from(Instant.now()));
            member.setCreatedAt(Timestamp.from(Instant.now()));
            member.setUpdatedAt(Timestamp.from(Instant.now()));
            projectMemberRepository.save(member);
        }

        return mapToResponse(saved);
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(UUID id, Long userId) {
        // Get user email
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String email = user.getEmail();

        // Check membership or pending invite
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(id, userId);
        boolean isInvited = projectInviteRepository.existsByProjectIdAndInvitedEmailAndStatus(id, email, "pending");

        if (!isMember && !isInvited) {
            throw new SecurityException("You are not authorized to view this project.");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        return mapToResponse(project);
    }


    public List<ProjectResponse> getProjectsByOwner(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse updateProject(UUID id, ProjectRequest request, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (!userId.equals(project.getOwnerId())) {
            throw new SecurityException("Only the project owner can update the project.");
        }

        if (request.getBudget() != null && request.getBudget().doubleValue() < 0) {
            throw new IllegalArgumentException("Project budget must be non-negative.");
        }

        if (request.getStartDate() != null && request.getDueDate() != null &&
                request.getStartDate().isAfter(request.getDueDate())) {
            throw new IllegalArgumentException("Start date cannot be after due date.");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setPriority(request.getPriority());
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());
        project.setBudget(request.getBudget());
        project.setTags(request.getTags());

        Project updated = projectRepository.save(project);
        return mapToResponse(updated);
    }

    public void deleteProject(UUID id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (!userId.equals(project.getOwnerId())) {
            throw new SecurityException("Only the project owner can delete the project.");
        }

        projectRepository.deleteById(id);
    }

    private ProjectResponse mapToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus());
        response.setPriority(project.getPriority());
        response.setStartDate(project.getStartDate());
        response.setDueDate(project.getDueDate());
        response.setBudget(project.getBudget());
        response.setTags(project.getTags());
        response.setOwnerId(project.getOwnerId());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        return response;
    }
}
