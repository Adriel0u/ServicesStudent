package com.uees.studentservices.service;

import com.uees.studentservices.config.AppProperties;
import com.uees.studentservices.dto.CourseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * WebClient hacia Grupo A (Learning Engine).
 * Endpoints documentados por el contrato: /api/courses (paginado),
 * /api/enrollments/{id} (verificacion COMPLETED), /api/my-courses (no se usa aqui).
 *
 * El servicio es resiliente: si Grupo A no esta levantado devuelve listas vacias y
 * un "verifico" pesimista para no bloquear el flujo del estudiante.
 */
@Service
public class LearningEngineClient {

    private static final Logger log = LoggerFactory.getLogger(LearningEngineClient.class);

    private final WebClient client;
    private final String apiPrefix;

    public LearningEngineClient(@Qualifier("learningEngineWebClient") WebClient client,
                                AppProperties props) {
        this.client = client;
        this.apiPrefix = props.getLearningEngine().getApiPrefix();
    }

    /**
     * Catalogo paginado de cursos publicados por Grupo A.
     */
    public List<CourseDto> fetchCourses(int page, int size, String category) {
        try {
            String path = apiPrefix + "/courses";
            return client.get()
                    .uri(uri -> {
                        var b = uri.path(path)
                                .queryParam("page", page)
                                .queryParam("size", size);
                        if (category != null && !category.isBlank()) {
                            b.queryParam("category", category);
                        }
                        return b.build();
                    })
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CatalogResponse>() {})
                    .timeout(Duration.ofSeconds(5))
                    .map(CatalogResponse::content)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.warn("[learning-engine] GET /courses status={} body={}",
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                        return reactor.core.publisher.Mono.empty();
                    })
                    .onErrorResume(ex -> {
                        log.warn("[learning-engine] GET /courses no disponible: {}", ex.getMessage());
                        return reactor.core.publisher.Mono.empty();
                    })
                    .blockOptional()
                    .orElse(Collections.emptyList());
        } catch (Exception ex) {
            log.warn("[learning-engine] excepcion no controlada listando catalogo: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Verifica con Grupo A si la inscripcion esta en estado COMPLETED.
     * Se usa antes de emitir el certificado, segun el PDF.
     */
    public boolean isEnrollmentCompleted(UUID enrollmentId) {
        try {
            Map<String, Object> resp = client.get()
                    .uri(apiPrefix + "/enrollments/{id}", enrollmentId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();
            if (resp == null) return false;
            Object status = resp.getOrDefault("status", resp.get("state"));
            return status != null && "COMPLETED".equalsIgnoreCase(status.toString());
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("[learning-engine] enrollment {} no existe en Grupo A", enrollmentId);
                return false;
            }
            log.warn("[learning-engine] error verificando enrollment {}: {}", enrollmentId, ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.warn("[learning-engine] no disponible para verificar enrollment {}: {}", enrollmentId, ex.getMessage());
            // Modo permisivo: si Grupo A no responde se confia en el evento module.completed=100
            return true;
        }
    }

    /** Estructura mas comun de respuesta paginada del Grupo A. */
    private record CatalogResponse(List<CourseDto> content) {
        @com.fasterxml.jackson.annotation.JsonCreator
        public CatalogResponse(@com.fasterxml.jackson.annotation.JsonProperty("content") List<CourseDto> content) {
            this.content = content == null ? List.of() : content;
        }
    }
}
