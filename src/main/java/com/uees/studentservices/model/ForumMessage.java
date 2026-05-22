package com.uees.studentservices.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "forum_messages",
        indexes = {
                @Index(name = "idx_forum_course", columnList = "course_id"),
                @Index(name = "idx_forum_course_created", columnList = "course_id,created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumMessage {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "course_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID courseId;

    @Column(name = "student_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID studentId;

    @Column(name = "student_name", nullable = false, length = 180)
    private String studentName;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
