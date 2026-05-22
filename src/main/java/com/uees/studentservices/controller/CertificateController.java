package com.uees.studentservices.controller;

import com.uees.studentservices.dto.CertificateDto;
import com.uees.studentservices.security.AuthUtils;
import com.uees.studentservices.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
@Tag(name = "Certificados", description = "Certificados emitidos al completar un curso al 100%")
public class CertificateController {

    private final CertificateService certificates;

    public CertificateController(CertificateService certificates) { this.certificates = certificates; }

    @Operation(summary = "Listado de certificados de un estudiante")
    @GetMapping("/{studentId}")
    public ResponseEntity<List<CertificateDto>> listByStudent(@PathVariable UUID studentId) {
        // Permitir al propio estudiante o a un admin (ROLE_ADMIN se manejaria con @PreAuthorize)
        UUID current = AuthUtils.currentPrincipal().getId();
        if (!current.equals(studentId) && !AuthUtils.currentPrincipal().getStudent().getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(certificates.listByStudent(studentId));
    }

    @Operation(summary = "Listado de certificados del estudiante autenticado")
    @GetMapping("/me")
    public ResponseEntity<List<CertificateDto>> myCertificates() {
        UUID current = AuthUtils.currentPrincipal().getId();
        return ResponseEntity.ok(certificates.listByStudent(current));
    }

    @Operation(summary = "Detalle de un certificado por id")
    @GetMapping("/detail/{id}")
    public ResponseEntity<CertificateDto> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(certificates.findById(id));
    }
}
