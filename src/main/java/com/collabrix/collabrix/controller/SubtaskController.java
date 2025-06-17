package com.collabrix.collabrix.controller;

import com.collabrix.collabrix.request.SubtaskRequest;
import com.collabrix.collabrix.service.SubtaskService;
import com.collabrix.collabrix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SubtaskController {

    @Autowired
    private SubtaskService subtaskService;

    @Autowired
    private UserService userService;

    @PostMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<?> createSubtask(@PathVariable UUID taskId,
                                           @RequestBody SubtaskRequest request,
                                           @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(subtaskService.createSubtask(taskId, request, userId));
    }

    @GetMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<?> getSubtasks(@PathVariable UUID taskId,
                                         @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(subtaskService.getSubtasksByTask(taskId, userId));
    }

    @DeleteMapping("/subtasks/{subtaskId}")
    public ResponseEntity<String> deleteSubtask(@PathVariable UUID subtaskId,
                                                @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        subtaskService.deleteSubtask(subtaskId, userId);
        return ResponseEntity.ok("Subtask deleted successfully.");
    }

    @PutMapping("/subtasks/{subtaskId}")
    public ResponseEntity<?> updateSubtaskCompletion(
            @PathVariable UUID subtaskId,
            @RequestBody Map<String, Boolean    > body,
            @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(subtaskService.updateSubtaskCompletion(subtaskId, body.get("completed"), userId));
    }

}

