package com.collabrix.collabrix.service;

import com.collabrix.collabrix.entity.Project;
import com.collabrix.collabrix.entity.Task;
import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.repository.ProjectMemberRepository;
import com.collabrix.collabrix.repository.ProjectRepository;
import com.collabrix.collabrix.repository.TaskRepository;
import com.collabrix.collabrix.repository.UserRepository;
import com.collabrix.collabrix.request.TaskRequest;
import com.collabrix.collabrix.response.TaskResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    public TaskResponse createTask(TaskRequest request, Long userId) {
        if (!isManagerOrOwner(userId, request.getProjectId())) {
            throw new AccessDeniedException("Only managers or owners can create tasks.");
        }



        Long assigneeId = userRepository.findByEmail(request.getAssigneeEmail()).getId();

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new EntityNotFoundException("Assignee not found"));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Creator not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setAssignee(assignee);
        task.setCreatedBy(creator);
        task.setProject(project);
        task.setCompleted(false);
        task.setStatus(request.getStatus() != null ? request.getStatus() : "To Do");

        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> getTasksByAssignee(Long userId) {
        return taskRepository.findByAssigneeId(userId)
                .stream().map(this::toResponse).toList();
    }

    public List<TaskResponse> getTasksByProject(UUID projectId, Long userId) {
        assertProjectMember(userId, projectId);
        return taskRepository.findByProjectId(projectId)
                .stream().map(this::toResponse).toList();
    }

    public TaskResponse getTaskById(UUID taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        assertProjectMember(userId, task.getProject().getId());
        return toResponse(task);
    }

    public TaskResponse updateTask(UUID taskId, TaskRequest request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        // Check if user is project owner or manager
        if (!isManagerOrOwner(userId, task.getProject().getId())) {
            throw new AccessDeniedException("Only managers or owners can update tasks.");
        }

        if(request.getAssigneeEmail()!=null){
            Long assigneeId = userRepository.findByEmail(request.getAssigneeEmail()).getId();

            // If assignee is changed, fetch and validate new assignee
            if (request.getAssigneeEmail() != null &&
                    !task.getAssignee().getId().equals(assigneeId)) {
                User newAssignee = userRepository.findById(assigneeId)
                        .orElseThrow(() -> new EntityNotFoundException("Assignee not found"));
                task.setAssignee(newAssignee);
            }
        }


        // Update other editable fields
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());

        return toResponse(taskRepository.save(task));
    }


    public TaskResponse updateTaskStatus(UUID taskId, String newStatus, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (!task.getAssignee().getId().equals(userId) && !isManagerOrOwner(userId, task.getProject().getId())) {
            throw new AccessDeniedException("Only the assignee or project owner can update the task status.");
        }

        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(UUID taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (!isManagerOrOwner(userId, task.getProject().getId())) {
            throw new AccessDeniedException("Only managers or owners can delete tasks.");
        }

        taskRepository.delete(task);
    }


    protected boolean isManagerOrOwner(Long userId, UUID projectId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(m -> {
                    String role = m.getRole().toLowerCase();
                    return role.equals("manager") || role.equals("owner");
                })
                .orElse(false);
    }

    private void assertProjectMember(Long userId, UUID projectId) {
        if (projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isEmpty()) {
            throw new AccessDeniedException("You are not a member of this project.");
        }
    }

    private TaskResponse toResponse(Task task) {

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(task.getId());
        taskResponse.setTitle(task.getTitle());
        taskResponse.setDescription(task.getDescription());
        taskResponse.setPriority(task.getPriority());
        taskResponse.setStatus(task.getStatus());
        taskResponse.setDueDate(task.getDueDate());
        taskResponse.setCompleted(task.isCompleted());
        taskResponse.setAssigneeName(task.getAssignee().getFullName());
        taskResponse.setAssigneeId(task.getAssignee().getId());
        taskResponse.setCreatedByName(task.getCreatedBy().getFullName());
        taskResponse.setCreatedById(task.getCreatedBy().getId());
        taskResponse.setProjectId(task.getProject().getId());

        return taskResponse;
    }
}

