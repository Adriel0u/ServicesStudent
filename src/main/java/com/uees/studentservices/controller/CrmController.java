package com.uees.studentservices.controller;

import com.uees.studentservices.security.AuthUtils;
import com.uees.studentservices.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/crm")
@Tag(name = "CRM", description = "Sincronizacion explicita con EspoCRM")
public class CrmController {

    private final CertificateService certificates;

    public CrmController(CertificateService certificates) { this.certificates = certificates; }

    @Operation(summary = "Forzar sincronizacion del estudiante actual con EspoCRM")
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync() {
        var p = AuthUtils.currentPrincipal();
        certificates.syncStudentToCrm(p.getId());
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Sincronizacion solicitada",
                "studentId", p.getId().toString(),
                "email", p.getEmail()
        ));
    }
}
