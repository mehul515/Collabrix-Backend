package com.collabrix.collabrix.service;

import com.collabrix.collabrix.entity.Comment;
import com.collabrix.collabrix.entity.Project;
import com.collabrix.collabrix.entity.Task;
import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.repository.CommentRepository;
import com.collabrix.collabrix.repository.ProjectMemberRepository;
import com.collabrix.collabrix.repository.TaskRepository;
import com.collabrix.collabrix.request.CommentRequest;
import com.collabrix.collabrix.response.CommentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    public CommentResponse addComment(CommentRequest request, Long userId) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Project project = task.getProject();
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);
        if (!isMember) {
            throw new RuntimeException("Only project members can comment");
        }

        User user = userService.getUserById(userId);

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(request.getContent());

        Comment saved = commentRepository.save(comment);
        return toResponse(saved);
    }

    public List<CommentResponse> getComments(UUID taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Project project = task.getProject();
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);
        if (!isMember) {
            throw new RuntimeException("Only project members can view comments");
        }

        return commentRepository.findByTaskId(taskId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CommentResponse updateComment(UUID commentId, String newContent, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Only the author can update this comment");
        }

        comment.setContent(newContent);
        Comment updated = commentRepository.save(comment);
        return toResponse(updated);
    }

    public void deleteComment(UUID commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Only the author can delete this comment");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse res = new CommentResponse();
        res.setId(comment.getId());
        res.setContent(comment.getContent());
        res.setAuthorName(comment.getUser().getFullName());
        res.setAuthorId(comment.getUser().getId());
        res.setCreatedAt(comment.getCreatedAt());
        res.setUpdatedAt(comment.getUpdatedAt());
        return res;
    }
}

