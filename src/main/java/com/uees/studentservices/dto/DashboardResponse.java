package com.uees.studentservices.dto;

import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        UUID studentId,
        String email,
        String fullName,
        String profilePictureUrl,
        long enrolledCoursesCount,
        long completedCoursesCount,
        int overallProgressPercent,
        List<EnrolledCourseDto> enrolledCourses,
        List<CertificateDto> certificates,
        List<CourseDto> availableCourses
) {}
