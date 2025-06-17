package com.collabrix.collabrix.controller;

import com.collabrix.collabrix.request.ProjectRequest;
import com.collabrix.collabrix.response.ProjectResponse;
import com.collabrix.collabrix.service.ProjectService;
import com.collabrix.collabrix.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @PostMapping()
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest request, @RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(projectService.createProject(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String jwt
    ) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(projectService.getProjectById(id, userId));
    }

    @GetMapping()
    public ResponseEntity<List<ProjectResponse>> getAllProject() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/owner")
    public ResponseEntity<List<ProjectResponse>> getProjectsByOwner(@RequestHeader("Authorization") String jwt) {
        Long ownerId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(projectService.getProjectsByOwner(ownerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @RequestBody ProjectRequest request,
            @RequestHeader("Authorization") String jwt
    ) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        return ResponseEntity.ok(projectService.updateProject(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String jwt
    ) {
        Long userId = userService.getUserProfile(jwt.substring(7)).getId();
        projectService.deleteProject(id, userId);
        return ResponseEntity.ok("Project with ID " + id + " has been successfully deleted.");
    }
}