package com.uees.studentservices.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Espejo local de una inscripcion (enrollment) reportada por Grupo A
 * via el evento RabbitMQ "enrollment.activated".
 * Aqui se calcula el % de progreso conforme llegan eventos "module.completed".
 */
@Entity
@Table(name = "enrollment_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_enrollment", columnNames = "enrollment_id")
        },
        indexes = {
                @Index(name = "idx_enrollment_student", columnList = "student_id"),
                @Index(name = "idx_enrollment_course", columnList = "course_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentProgress {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "enrollment_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID enrollmentId;

    @Column(name = "student_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID studentId;

    @Column(name = "student_email", nullable = false, length = 180)
    private String studentEmail;

    @Column(name = "student_name", length = 180)
    private String studentName;

    @Column(name = "course_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID courseId;

    @Column(name = "course_name", length = 250)
    private String courseName;

    @Column(name = "progress_percent", nullable = false)
    @Builder.Default
    private Integer progressPercent = 0;

    @Column(name = "modules_completed", nullable = false)
    @Builder.Default
    private Integer modulesCompleted = 0;

    @Column(name = "completed")
    @Builder.Default
    private boolean completed = false;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
