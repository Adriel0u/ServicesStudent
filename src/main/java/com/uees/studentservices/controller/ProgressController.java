package com.uees.studentservices.controller;

import com.uees.studentservices.dto.PageResponse;
import com.uees.studentservices.dto.ProgressResponse;
import com.uees.studentservices.dto.RankingEntryDto;
import com.uees.studentservices.security.AuthUtils;
import com.uees.studentservices.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/progress")
@Tag(name = "Progreso", description = "Seguimiento de progreso por curso y ranking de estudiantes")
public class ProgressController {

    private final ProgressService progress;

    public ProgressController(ProgressService progress) { this.progress = progress; }

    @Operation(summary = "Progreso del estudiante autenticado en un curso")
    @GetMapping("/{courseId}")
    public ResponseEntity<ProgressResponse> byCourse(@PathVariable UUID courseId) {
        UUID studentId = AuthUtils.currentPrincipal().getId();
        return ResponseEntity.ok(progress.getByCourseForStudent(studentId, courseId));
    }

    @Operation(summary = "Ranking de estudiantes por progreso en un curso (Pageable)")
    @GetMapping("/{courseId}/ranking")
    public ResponseEntity<PageResponse<RankingEntryDto>> ranking(
            @PathVariable UUID courseId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(progress.rankingByCourse(courseId, pageable)));
    }
}
