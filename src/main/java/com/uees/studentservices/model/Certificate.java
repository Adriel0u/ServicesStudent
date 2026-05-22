package com.uees.studentservices.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_certificate_enrollment", columnNames = "enrollment_id")
        },
        indexes = {
                @Index(name = "idx_certificate_student", columnList = "student_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "certificate_code", nullable = false, unique = true, length = 60)
    private String certificateCode;

    @Column(name = "enrollment_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID enrollmentId;

    @Column(name = "student_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID studentId;

    @Column(name = "student_email", nullable = false, length = 180)
    private String studentEmail;

    @Column(name = "student_name", nullable = false, length = 180)
    private String studentName;

    @Column(name = "course_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID courseId;

    @Column(name = "course_name", nullable = false, length = 250)
    private String courseName;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "email_sent", nullable = false)
    @Builder.Default
    private boolean emailSent = false;

    @PrePersist
    void onCreate() {
        if (this.issuedAt == null) {
            this.issuedAt = LocalDateTime.now();
        }
    }
}
