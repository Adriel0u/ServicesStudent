package com.uees.studentservices.dto;

import com.uees.studentservices.model.ForumMessage;

import java.time.LocalDateTime;
import java.util.UUID;

public record ForumMessageDto(
        UUID id,
        UUID courseId,
        UUID studentId,
        String studentName,
        String profilePictureUrl,
        String content,
        LocalDateTime createdAt
) {
    public static ForumMessageDto fromEntity(ForumMessage m) {
        return new ForumMessageDto(
                m.getId(),
                m.getCourseId(),
                m.getStudentId(),
                m.getStudentName(),
                m.getProfilePictureUrl(),
                m.getContent(),
                m.getCreatedAt()
        );
    }
}
