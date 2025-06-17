package com.collabrix.collabrix.repository;

import com.collabrix.collabrix.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    List<ProjectMember> findByProjectId(UUID projectId);
    List<ProjectMember> findByUserId(Long userId);
    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, Long userId);
    boolean existsByProjectIdAndUserId(UUID projectId, Long userId);
}

