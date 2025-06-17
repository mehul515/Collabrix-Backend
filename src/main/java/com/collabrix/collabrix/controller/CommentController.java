package com.collabrix.collabrix.controller;

import com.collabrix.collabrix.request.CommentRequest;
import com.collabrix.collabrix.request.UpdateCommentRequest;
import com.collabrix.collabrix.response.CommentResponse;
import com.collabrix.collabrix.service.CommentService;
import com.collabrix.collabrix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @RequestBody CommentRequest request,
            @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(commentService.addComment(request, userId));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID taskId,
            @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(commentService.getComments(taskId, userId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @RequestBody UpdateCommentRequest request,
            @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(commentService.updateComment(commentId, request.getContent(), userId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable UUID commentId,
            @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("Comment deleted successfully.");
    }
}

