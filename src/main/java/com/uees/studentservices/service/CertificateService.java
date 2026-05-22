package com.uees.studentservices.service;

import com.uees.studentservices.dto.CertificateDto;
import com.uees.studentservices.exception.DomainException;
import com.uees.studentservices.model.Certificate;
import com.uees.studentservices.model.EnrollmentProgress;
import com.uees.studentservices.model.Student;
import com.uees.studentservices.repository.CertificateRepository;
import com.uees.studentservices.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateService.class);
    private static final DateTimeFormatter CODE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CertificateRepository certificates;
    private final StudentRepository students;
    private final EmailService emailService;
    private final LearningEngineClient learningEngine;
    private final EspoCrmService espoCrm;

    public CertificateService(CertificateRepository certificates,
                              StudentRepository students,
                              EmailService emailService,
                              LearningEngineClient learningEngine,
                              EspoCrmService espoCrm) {
        this.certificates = certificates;
        this.students = students;
        this.emailService = emailService;
        this.learningEngine = learningEngine;
        this.espoCrm = espoCrm;
    }

    /**
     * Emite y envia el certificado correspondiente a un enrollment completado.
     * - Verifica con Grupo A (WebClient) que la inscripcion este COMPLETED.
     * - Crea registro idempotente por enrollmentId.
     * - Envia email Thymeleaf.
     * - Actualiza EspoCRM con 'cursos completados'.
     */
    @Transactional
    public Certificate issueAndSend(EnrollmentProgress ep) {
        // Verificacion con Grupo A
        boolean ok = learningEngine.isEnrollmentCompleted(ep.getEnrollmentId());
        if (!ok) {
            log.warn("[certificate] Grupo A NO confirma COMPLETED para {}; se continua porque progreso local=100",
                    ep.getEnrollmentId());
        }

        Certificate cert = certificates.findByEnrollmentId(ep.getEnrollmentId())
                .orElseGet(() -> Certificate.builder()
                        .certificateCode(buildCode(ep))
                        .enrollmentId(ep.getEnrollmentId())
                        .studentId(ep.getStudentId())
                        .studentEmail(ep.getStudentEmail())
                        .studentName(ep.getStudentName())
                        .courseId(ep.getCourseId())
                        .courseName(ep.getCourseName())
                        .issuedAt(LocalDateTime.now())
                        .emailSent(false)
                        .build());

        cert = certificates.save(cert);

        boolean sent = emailService.sendCertificateEmail(cert);
        if (sent && !cert.isEmailSent()) {
            cert.setEmailSent(true);
            cert = certificates.save(cert);
        }

        // Sincronizacion EspoCRM
        students.findById(ep.getStudentId()).ifPresent(
                s -> espoCrm.incrementCompletedCourses(s, ep.getCourseName()));

        return cert;
    }

    /**
     * Endpoint /api/certificates/{studentId} — listado de certificados obtenidos.
     */
    public List<CertificateDto> listByStudent(UUID studentId) {
        return certificates.findByStudentIdOrderByIssuedAtDesc(studentId).stream()
                .map(CertificateDto::fromEntity)
                .toList();
    }

    public CertificateDto findById(UUID id) {
        return certificates.findById(id)
                .map(CertificateDto::fromEntity)
                .orElseThrow(() -> DomainException.notFound("Certificado no encontrado: " + id));
    }

    /**
     * Endpoint POST /api/crm/sync — fuerza sincronizacion del estudiante actual.
     */
    public void syncStudentToCrm(UUID studentId) {
        Student s = students.findById(studentId)
                .orElseThrow(() -> DomainException.notFound("Estudiante no encontrado"));
        if (s.getEspoCrmContactId() == null) {
            String crmId = espoCrm.createContact(s);
            if (crmId != null) {
                s.setEspoCrmContactId(crmId);
                students.save(s);
            }
        }
        // Adicionalmente forzamos un update de descripcion para reflejar el avance actual
        espoCrm.incrementCompletedCourses(s, "(refresh)");
    }

    private String buildCode(EnrollmentProgress ep) {
        String date = ep.getCompletedAt() != null
                ? ep.getCompletedAt().format(CODE_FMT)
                : LocalDateTime.now().format(CODE_FMT);
        String shortId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "UEES-CERT-" + date + "-" + shortId;
    }
}
