package com.collabrix.collabrix.service;

import com.collabrix.collabrix.entity.ProjectMember;
import com.collabrix.collabrix.repository.ProjectMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectMemberService {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    public List<ProjectMember> getProjectMembers(UUID projectId, Long requesterId) {
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, requesterId);
        if (!isMember) {
            throw new SecurityException("You are not a member of this project.");
        }

        return projectMemberRepository.findByProjectId(projectId);
    }


    public List<ProjectMember> getUserMemberships(Long userId) {
        return projectMemberRepository.findByUserId(userId);
    }

}
