package com.collabrix.collabrix.service;

import com.collabrix.collabrix.entity.Subtask;
import com.collabrix.collabrix.entity.Task;
import com.collabrix.collabrix.repository.ProjectMemberRepository;
import com.collabrix.collabrix.repository.SubtaskRepository;
import com.collabrix.collabrix.repository.TaskRepository;
import com.collabrix.collabrix.request.SubtaskRequest;
import com.collabrix.collabrix.response.SubtaskResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SubtaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TaskService taskService;


    public SubtaskResponse createSubtask(UUID taskId, SubtaskRequest request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        UUID projectId = task.getProject().getId();

        boolean isManagerOrOwner = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(m -> {
                    String role = m.getRole().toLowerCase();
                    return role.equals("manager") || role.equals("owner");
                })
                .orElse(false);

        boolean isAssignee = task.getAssignee().getId().equals(userId);

        if (!(isManagerOrOwner || isAssignee)) {
            throw new AccessDeniedException("Only the assignee or project manager/owner can create subtasks.");
        }

        Subtask subtask = new Subtask();
        subtask.setTitle(request.getTitle());
        subtask.setCompleted(false);
        subtask.setTask(task);

        return toResponse(subtaskRepository.save(subtask));
    }


    public List<SubtaskResponse> getSubtasksByTask(UUID taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        assertProjectMember(userId, task.getProject().getId());

        return subtaskRepository.findByTaskId(taskId).stream()
                .map(this::toResponse).toList();
    }


    public void deleteSubtask(UUID subtaskId, Long userId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found"));

        Task task = subtask.getTask();

        UUID projectId = task.getProject().getId();

        boolean isManagerOrOwner = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(m -> {
                    String role = m.getRole().toLowerCase();
                    return role.equals("manager") || role.equals("owner");
                })
                .orElse(false);

        boolean isAssignee = task.getAssignee().getId().equals(userId);


        if (!isManagerOrOwner && !isAssignee) {
            throw new RuntimeException("Only owner, manager, or assignee can delete the subtask");
        }

        subtaskRepository.delete(subtask);
    }

    public SubtaskResponse updateSubtaskCompletion(UUID subtaskId, Boolean isCompleted, Long userId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found"));

        // Optional: Check if user is authorized (owner, manager, or assignee of the parent task)
        Task task = subtask.getTask();
        if (!task.getAssignee().getId().equals(userId) && !taskService.isManagerOrOwner(userId, task.getProject().getId())) {
            throw new RuntimeException("You are not authorized to update this subtask");
        }

        subtask.setCompleted(isCompleted);
        Subtask updated = subtaskRepository.save(subtask);
        return toResponse(updated);
    }



    private void assertProjectMember(Long userId, UUID projectId) {
        if (projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isEmpty()) {
            throw new AccessDeniedException("You are not a member of this project.");
        }
    }

    private SubtaskResponse toResponse(Subtask subtask) {
        SubtaskResponse response = new SubtaskResponse();
        response.setId(subtask.getId());
        response.setTitle(subtask.getTitle());
        response.setCompleted(subtask.isCompleted());
        return response;
    }
}
