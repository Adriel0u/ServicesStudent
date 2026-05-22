package com.uees.studentservices.messaging;

import com.uees.studentservices.dto.events.EnrollmentActivatedEvent;
import com.uees.studentservices.dto.events.ModuleCompletedEvent;
import com.uees.studentservices.service.ProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventListener.class);

    private final ProgressService progressService;

    public EnrollmentEventListener(ProgressService progressService) {
        this.progressService = progressService;
    }

    @RabbitListener(queues = "${app.rabbit.enrollment-activated-queue}")
    public void onEnrollmentActivated(EnrollmentActivatedEvent event) {
        log.info("[rabbit] <- enrollment.activated enrollmentId={} student={} course={}",
                event.enrollmentId(), event.studentEmail(), event.courseName());
        try {
            if (event.enrollmentId() == null || event.studentId() == null || event.courseId() == null) {
                throw new IllegalArgumentException("Evento enrollment.activated incompleto: " + event);
            }
            progressService.applyEnrollmentActivated(event);
        } catch (IllegalArgumentException ex) {
            log.warn("[rabbit] descartando enrollment.activated invalido a DLQ: {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("[rabbit] error procesando enrollment.activated: {}", ex.getMessage(), ex);
            throw ex; // retry/DLQ por configuracion
        }
    }

    @RabbitListener(queues = "${app.rabbit.module-completed-queue}")
    public void onModuleCompleted(ModuleCompletedEvent event) {
        log.info("[rabbit] <- module.completed enrollmentId={} moduleId={} percent={}",
                event.enrollmentId(), event.moduleId(), event.completionPercent());
        try {
            if (event.enrollmentId() == null || event.moduleId() == null) {
                throw new IllegalArgumentException("Evento module.completed incompleto: " + event);
            }
            progressService.applyModuleCompleted(event);
        } catch (IllegalArgumentException ex) {
            log.warn("[rabbit] descartando module.completed invalido a DLQ: {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("[rabbit] error procesando module.completed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
