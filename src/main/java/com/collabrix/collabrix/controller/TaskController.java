package com.collabrix.collabrix.controller;

import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.request.TaskRequest;
import com.collabrix.collabrix.response.TaskResponse;
import com.collabrix.collabrix.service.TaskService;
import com.collabrix.collabrix.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request,
                                        @RequestHeader("Authorization") String jwt) {
        User userDetails = userService.getUserProfile(jwt.substring(7));
        Long userId = userDetails.getId();
        return ResponseEntity.ok(taskService.createTask(request, userId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getByProject(@PathVariable UUID projectId,
                                          @RequestHeader("Authorization") String jwt) {
        User userDetails = userService.getUserProfile(jwt.substring(7));
        Long userId = userDetails.getId();
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable UUID id,
                                     @RequestHeader("Authorization") String jwt) {
        User userDetails = userService.getUserProfile(jwt.substring(7));
        Long userId = userDetails.getId();
        return ResponseEntity.ok(taskService.getTaskById(id, userId));
    }

    @GetMapping("/myTasks")
    public ResponseEntity<List<TaskResponse>> getMyTasks(@RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(taskService.getTasksByAssignee(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable UUID id,
                                        @RequestBody TaskRequest request,
                                        @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(taskService.updateTask(id, request, userId));
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable UUID id,
                                              @RequestBody Map<String, String> body,
                                              @RequestHeader("Authorization") String jwt) {
        User userDetails = userService.getUserProfile(jwt.substring(7));
        Long userId = userDetails.getId();
        String newStatus = body.get("status");
        return ResponseEntity.ok(taskService.updateTaskStatus(id, newStatus, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable UUID id,
                                        @RequestHeader("Authorization") String jwt) {
        User userDetails = userService.getUserProfile(jwt.substring(7));
        Long userId = userDetails.getId();
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }
}

