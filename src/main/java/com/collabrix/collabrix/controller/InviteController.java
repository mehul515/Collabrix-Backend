package com.collabrix.collabrix.controller;

import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.request.ProjectInviteRequest;
import com.collabrix.collabrix.response.ProjectInviteResponse;
import com.collabrix.collabrix.service.ProjectInviteService;
import com.collabrix.collabrix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    @Autowired
    private ProjectInviteService projectInviteService;

    @Autowired
    private UserService userService;

    @PostMapping("/send")
    public ResponseEntity<ProjectInviteResponse> sendInvite(
            @RequestHeader("Authorization") String jwt,
            @RequestBody ProjectInviteRequest request
    ) {
        Long inviterId = userService.getUserProfile(jwt.substring(7)).getId();
        System.out.println(request);
        return ResponseEntity.ok(projectInviteService.sendInvite(inviterId, request));
    }

    @PostMapping("/{inviteId}/accept")
    public ResponseEntity<ProjectInviteResponse> acceptInvite(
            @PathVariable UUID inviteId,
            @RequestHeader("Authorization") String jwt
    ) {
        User user = userService.getUserProfile(jwt.substring(7));
        Long userId = user.getId();
        String userEmail = user.getEmail();
        return ResponseEntity.ok(projectInviteService.acceptInvite(inviteId, userId, userEmail));
    }

    @PostMapping("/{inviteId}/decline")
    public ResponseEntity<ProjectInviteResponse> declineInvite(
            @PathVariable UUID inviteId,
            @RequestHeader("Authorization") String jwt
    ) {
        User user = userService.getUserProfile(jwt.substring(7));
        Long userId = user.getId();
        String userEmail = user.getEmail();
        return ResponseEntity.ok(projectInviteService.declineInvite(inviteId, userId, userEmail));
    }


    @GetMapping
    public ResponseEntity<List<ProjectInviteResponse>> getUserInvites(
            @RequestHeader("Authorization") String jwt
    ) {
        String email = userService.getUserProfile(jwt.substring(7)).getEmail();
        return ResponseEntity.ok(projectInviteService.getUserInvites(email));
    }
}

