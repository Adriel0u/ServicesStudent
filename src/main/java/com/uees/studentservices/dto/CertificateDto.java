package com.uees.studentservices.dto;

import com.uees.studentservices.model.Certificate;

import java.time.LocalDateTime;
import java.util.UUID;

public record CertificateDto(
        UUID id,
        String certificateCode,
        UUID enrollmentId,
        UUID studentId,
        String studentEmail,
        String studentName,
        UUID courseId,
        String courseName,
        LocalDateTime issuedAt,
        boolean emailSent
) {
    public static CertificateDto fromEntity(Certificate c) {
        return new CertificateDto(
                c.getId(),
                c.getCertificateCode(),
                c.getEnrollmentId(),
                c.getStudentId(),
                c.getStudentEmail(),
                c.getStudentName(),
                c.getCourseId(),
                c.getCourseName(),
                c.getIssuedAt(),
                c.isEmailSent()
        );
    }
}
