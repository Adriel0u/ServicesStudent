package com.uees.studentservices.service;

import com.uees.studentservices.dto.ForumMessageDto;
import com.uees.studentservices.model.ForumMessage;
import com.uees.studentservices.model.Student;
import com.uees.studentservices.repository.ForumMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ForumService {

    private static final Logger log = LoggerFactory.getLogger(ForumService.class);
    private static final String TOPIC_TEMPLATE = "/topic/course/%s/forum";

    private final ForumMessageRepository messages;
    private final SimpMessagingTemplate broker;

    public ForumService(ForumMessageRepository messages, SimpMessagingTemplate broker) {
        this.messages = messages;
        this.broker = broker;
    }

    /**
     * Persiste el mensaje y lo difunde por /topic/course/{courseId}/forum
     */
    @Transactional
    public ForumMessageDto post(UUID courseId, Student author, String content) {
        ForumMessage msg = ForumMessage.builder()
                .courseId(courseId)
                .studentId(author.getId())
                .studentName(author.getFullName())
                .profilePictureUrl(author.getProfilePictureUrl())
                .content(content)
                .build();
        msg = messages.save(msg);

        ForumMessageDto dto = ForumMessageDto.fromEntity(msg);
        String dest = String.format(TOPIC_TEMPLATE, courseId);
        broker.convertAndSend(dest, dto);
        log.info("[forum] {} publico en {} ({} chars)", author.getEmail(), dest, content.length());
        return dto;
    }

    public Page<ForumMessageDto> history(UUID courseId, Pageable pageable) {
        return messages.findByCourseIdOrderByCreatedAtDesc(courseId, pageable)
                .map(ForumMessageDto::fromEntity);
    }
}
