package com.uees.studentservices.controller;

import com.uees.studentservices.dto.ForumMessageDto;
import com.uees.studentservices.dto.ForumMessageRequest;
import com.uees.studentservices.dto.PageResponse;
import com.uees.studentservices.security.AuthUtils;
import com.uees.studentservices.service.ForumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses/{courseId}/forum")
@Tag(name = "Foro", description = "Foro de discusion por curso (historial REST + WebSocket en /topic/course/{id}/forum)")
public class ForumController {

    private final ForumService forum;

    public ForumController(ForumService forum) { this.forum = forum; }

    @Operation(summary = "Historial paginado del foro de un curso")
    @GetMapping
    public ResponseEntity<PageResponse<ForumMessageDto>> history(
            @PathVariable UUID courseId,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(forum.history(courseId, pageable)));
    }

    @Operation(summary = "Publicar mensaje en el foro (lo difunde tambien via WebSocket STOMP)")
    @PostMapping
    public ResponseEntity<ForumMessageDto> post(@PathVariable UUID courseId,
                                                @Valid @RequestBody ForumMessageRequest req) {
        return ResponseEntity.ok(forum.post(courseId, AuthUtils.currentPrincipal().getStudent(), req.content()));
    }
}
