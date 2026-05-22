package com.uees.studentservices.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra cada modulo completado por un estudiante en un enrollment.
 * Sirve para evitar duplicados cuando llegan eventos repetidos por reintentos.
 */
@Entity
@Table(name = "module_completions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_module_completion",
                        columnNames = {"enrollment_id", "module_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleCompletion {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "enrollment_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID enrollmentId;

    @Column(name = "module_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID moduleId;

    @Column(name = "module_name", length = 250)
    private String moduleName;

    @Column(name = "completion_percent")
    private Integer completionPercent;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        if (this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
}
