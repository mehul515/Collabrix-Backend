package com.collabrix.collabrix.repository;

import com.collabrix.collabrix.entity.ProjectInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectInviteRepository extends JpaRepository<ProjectInvite, UUID> {
    List<ProjectInvite> findByInvitedEmail(String email);
    List<ProjectInvite> findByProjectId(UUID projectId);
    Optional<ProjectInvite> findByProjectIdAndInvitedEmail(UUID projectId, String email);
    boolean existsByProjectIdAndInvitedEmailAndStatus(UUID projectId, String invitedEmail, String status);
}

