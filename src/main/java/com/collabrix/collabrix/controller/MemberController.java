package com.collabrix.collabrix.controller;

import com.collabrix.collabrix.entity.ProjectMember;
import com.collabrix.collabrix.service.ProjectMemberService;
import com.collabrix.collabrix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private UserService userService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProjectMember>> getProjectMembers(
            @PathVariable UUID projectId,
            @RequestHeader("Authorization") String jwt
    ) {
        Long requesterId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId, requesterId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ProjectMember>> getUserProjects(
            @RequestHeader("Authorization") String jwt
    ) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(projectMemberService.getUserMemberships(userId));
    }

}

