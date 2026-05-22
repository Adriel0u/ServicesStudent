package com.uees.studentservices.service;

import com.uees.studentservices.dto.ProgressResponse;
import com.uees.studentservices.dto.RankingEntryDto;
import com.uees.studentservices.dto.events.EnrollmentActivatedEvent;
import com.uees.studentservices.dto.events.ModuleCompletedEvent;
import com.uees.studentservices.exception.DomainException;
import com.uees.studentservices.model.EnrollmentProgress;
import com.uees.studentservices.model.ModuleCompletion;
import com.uees.studentservices.repository.EnrollmentProgressRepository;
import com.uees.studentservices.repository.ModuleCompletionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ProgressService {

    private static final Logger log = LoggerFactory.getLogger(ProgressService.class);

    private final EnrollmentProgressRepository progressRepo;
    private final ModuleCompletionRepository moduleRepo;
    private final EmailService email;
    private final CertificateService certificateService;

    public ProgressService(EnrollmentProgressRepository progressRepo,
                           ModuleCompletionRepository moduleRepo,
                           EmailService email,
                           CertificateService certificateService) {
        this.progressRepo = progressRepo;
        this.moduleRepo = moduleRepo;
        this.email = email;
        this.certificateService = certificateService;
    }

    /**
     * Aplica el evento "enrollment.activated" recibido del Grupo A.
     * - Crea el espejo local con progreso 0%.
     * - Envia email de bienvenida del curso.
     */
    @Transactional
    public EnrollmentProgress applyEnrollmentActivated(EnrollmentActivatedEvent event) {
        EnrollmentProgress ep = progressRepo.findByEnrollmentId(event.enrollmentId())
                .orElseGet(() -> EnrollmentProgress.builder()
                        .enrollmentId(event.enrollmentId())
                        .build());

        ep.setStudentId(event.studentId());
        ep.setStudentEmail(event.studentEmail());
        ep.setStudentName(event.studentName());
        ep.setCourseId(event.courseId());
        ep.setCourseName(event.courseName());
        ep.setProgressPercent(0);
        ep.setModulesCompleted(0);
        ep.setCompleted(false);
        ep.setCompletedAt(null);
        ep.setActivatedAt(event.activatedAt() != null
                ? event.activatedAt().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
                : LocalDateTime.now());

        ep = progressRepo.save(ep);
        log.info("[progress] enrollment {} (student={} course={}) activado",
                event.enrollmentId(), event.studentEmail(), event.courseName());

        if (event.studentEmail() != null && event.courseName() != null) {
            email.sendEnrollmentWelcome(event.studentEmail(), event.studentName(), event.courseName());
        }
        return ep;
    }

    /**
     * Aplica el evento "module.completed".
     * - Idempotente por (enrollmentId, moduleId).
     * - Actualiza % progreso = max(actual, completionPercent).
     * - Si llega a 100% emite certificado y email.
     */
    @Transactional
    public EnrollmentProgress applyModuleCompleted(ModuleCompletedEvent event) {
        EnrollmentProgress ep = progressRepo.findByEnrollmentId(event.enrollmentId())
                .orElseThrow(() -> DomainException.notFound(
                        "No existe enrollment activado " + event.enrollmentId()
                                + " (espera evento enrollment.activated primero)"));

        if (moduleRepo.existsByEnrollmentIdAndModuleId(event.enrollmentId(), event.moduleId())) {
            log.debug("[progress] modulo {} ya estaba registrado en enrollment {} (idempotencia)",
                    event.moduleId(), event.enrollmentId());
            return ep;
        }

        moduleRepo.save(ModuleCompletion.builder()
                .enrollmentId(event.enrollmentId())
                .moduleId(event.moduleId())
                .moduleName(event.moduleName())
                .completionPercent(event.completionPercent())
                .completedAt(LocalDateTime.now())
                .build());

        long modulesDone = moduleRepo.countByEnrollmentId(event.enrollmentId());
        ep.setModulesCompleted((int) modulesDone);

        int newPercent = event.completionPercent() == null ? ep.getProgressPercent() : event.completionPercent();
        newPercent = Math.max(0, Math.min(100, newPercent));
        ep.setProgressPercent(Math.max(ep.getProgressPercent(), newPercent));

        if (ep.getProgressPercent() >= 100 && !ep.isCompleted()) {
            ep.setCompleted(true);
            ep.setCompletedAt(LocalDateTime.now());
            ep = progressRepo.save(ep);
            log.info("[progress] enrollment {} alcanzo 100% -> emitiendo certificado", ep.getEnrollmentId());
            certificateService.issueAndSend(ep);
            return ep;
        }

        ep = progressRepo.save(ep);
        log.info("[progress] enrollment {} progreso={}% modules={}",
                ep.getEnrollmentId(), ep.getProgressPercent(), ep.getModulesCompleted());
        return ep;
    }

    public ProgressResponse getByCourseForStudent(UUID studentId, UUID courseId) {
        EnrollmentProgress ep = progressRepo.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> DomainException.notFound(
                        "No tienes inscripcion activa en el curso " + courseId));
        return toResponse(ep);
    }

    public List<ProgressResponse> listForStudent(UUID studentId) {
        return progressRepo.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<RankingEntryDto> rankingByCourse(UUID courseId, Pageable pageable) {
        Page<EnrollmentProgress> page = progressRepo
                .findByCourseIdOrderByProgressPercentDescUpdatedAtAsc(courseId, pageable);
        int base = (int) page.getPageable().getOffset();
        return page.map(ep -> new RankingEntryDto(
                base + (int) (page.getContent().indexOf(ep) + 1),
                ep.getStudentId(),
                ep.getStudentName(),
                ep.getStudentEmail(),
                ep.getProgressPercent(),
                ep.isCompleted()
        ));
    }

    public ProgressResponse toResponse(EnrollmentProgress ep) {
        return new ProgressResponse(
                ep.getEnrollmentId(),
                ep.getStudentId(),
                ep.getCourseId(),
                ep.getCourseName(),
                ep.getProgressPercent(),
                ep.getModulesCompleted(),
                ep.isCompleted(),
                ep.getActivatedAt(),
                ep.getCompletedAt(),
                ep.getUpdatedAt()
        );
    }
}
