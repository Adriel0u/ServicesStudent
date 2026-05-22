package com.uees.studentservices.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Vista de un curso publicado por el catalogo de Grupo A.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CourseDto(
        UUID id,
        String title,
        String description,
        String imageUrl,
        String instructor,
        String category,
        BigDecimal price
) {}
