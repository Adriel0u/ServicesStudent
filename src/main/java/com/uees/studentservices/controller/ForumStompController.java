package com.uees.studentservices.controller;

import com.uees.studentservices.dto.ForumMessageDto;
import com.uees.studentservices.dto.ForumMessageRequest;
import com.uees.studentservices.model.Student;
import com.uees.studentservices.repository.StudentRepository;
import com.uees.studentservices.service.ForumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * Permite publicar mensajes via WebSocket STOMP:
 *   SEND /app/course/{courseId}/forum
 * El mensaje persistido se difunde por /topic/course/{courseId}/forum (gestionado por ForumService).
 */
@Controller
public class ForumStompController {

    private static final Logger log = LoggerFactory.getLogger(ForumStompController.class);

    private final ForumService forum;
    private final StudentRepository students;

    public ForumStompController(ForumService forum, StudentRepository students) {
        this.forum = forum;
        this.students = students;
    }

    @MessageMapping("/course/{courseId}/forum")
    @SendToUser("/queue/forum-ack")
    public ForumMessageDto publish(@DestinationVariable UUID courseId,
                                   @Payload ForumMessageRequest req,
                                   Principal principal) {
        if (principal == null || principal.getName() == null) {
            log.warn("[ws] mensaje sin principal; se ignora");
            return null;
        }
        Student author = students.findByEmailIgnoreCase(principal.getName()).orElse(null);
        if (author == null) {
            log.warn("[ws] principal {} no encontrado en BD", principal.getName());
            return null;
        }
        return forum.post(courseId, author, req.content());
    }
}
