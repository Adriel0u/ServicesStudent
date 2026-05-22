package com.uees.studentservices.controller;

import com.uees.studentservices.dto.DashboardResponse;
import com.uees.studentservices.security.AuthUtils;
import com.uees.studentservices.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard personal del estudiante (cursos + progreso + certificados + catalogo)")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }

    @Operation(summary = "Dashboard del estudiante autenticado")
    @GetMapping
    public ResponseEntity<DashboardResponse> me() {
        return ResponseEntity.ok(dashboard.buildFor(AuthUtils.currentPrincipal().getId()));
    }
}
