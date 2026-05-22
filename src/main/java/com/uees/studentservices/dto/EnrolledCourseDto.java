package com.uees.studentservices.dto;

import java.util.UUID;

public record EnrolledCourseDto(
        UUID enrollmentId,
        UUID courseId,
        String courseName,
        Integer progressPercent,
        Integer modulesCompleted,
        boolean completed,
        String imageUrl,
        String instructor
) {}
