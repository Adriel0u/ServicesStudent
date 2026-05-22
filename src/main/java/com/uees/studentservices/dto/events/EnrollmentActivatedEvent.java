package com.uees.studentservices.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Contrato compartido (PDF):
 *   exchange: enrollments.exchange
 *   routing:  enrollment.activated
 *   {
 *     "enrollmentId": "UUID",
 *     "studentId":    "UUID",
 *     "studentEmail": "string",
 *     "studentName":  "string",
 *     "courseId":     "UUID",
 *     "courseName":   "string",
 *     "activatedAt":  "ISO-8601"
 *   }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnrollmentActivatedEvent(
        UUID enrollmentId,
        UUID studentId,
        String studentEmail,
        String studentName,
        UUID courseId,
        String courseName,
        OffsetDateTime activatedAt
) {}
