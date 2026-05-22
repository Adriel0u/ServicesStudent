package com.uees.studentservices.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProgressResponse(
        UUID enrollmentId,
        UUID studentId,
        UUID courseId,
        String courseName,
        Integer progressPercent,
        Integer modulesCompleted,
        boolean completed,
        LocalDateTime activatedAt,
        LocalDateTime completedAt,
        LocalDateTime updatedAt
) {}
