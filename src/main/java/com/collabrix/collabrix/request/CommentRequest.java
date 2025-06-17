package com.collabrix.collabrix.request;

import java.util.UUID;

public class CommentRequest {
    private UUID taskId;
    private String content;

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

