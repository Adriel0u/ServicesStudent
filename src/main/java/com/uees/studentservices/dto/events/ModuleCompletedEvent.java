package com.uees.studentservices.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Contrato compartido (PDF):
 *   exchange: enrollments.exchange
 *   routing:  module.completed
 *   {
 *     "enrollmentId":      "UUID",
 *     "moduleId":          "UUID",
 *     "moduleName":        "string",
 *     "completionPercent": int
 *   }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ModuleCompletedEvent(
        UUID enrollmentId,
        UUID moduleId,
        String moduleName,
        Integer completionPercent
) {}
