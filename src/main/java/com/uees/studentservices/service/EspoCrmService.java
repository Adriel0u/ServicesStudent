package com.uees.studentservices.service;

import com.uees.studentservices.config.AppProperties;
import com.uees.studentservices.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Sincronizacion con EspoCRM vis WebClient.
 * - Al registrar estudiante: crear contacto.
 * - Al completar curso: actualizar campo 'cursos completados' + incrementar contador.
 *
 * Si el servicio esta deshabilitado o no responde, no se rompe el flujo principal.
 */
@Service
public class EspoCrmService {

    private static final Logger log = LoggerFactory.getLogger(EspoCrmService.class);

    private final WebClient client;
    private final AppProperties props;

    public EspoCrmService(@Qualifier("espoCrmWebClient") WebClient client, AppProperties props) {
        this.client = client;
        this.props = props;
    }

    /**
     * Crea un Contact en EspoCRM. Devuelve el id de EspoCRM o null si fallo.
     */
    public String createContact(Student student) {
        if (!props.getEspocrm().isEnabled()) {
            log.info("[espocrm] deshabilitado; saltando createContact para {}", student.getEmail());
            return null;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            String fullName = student.getFullName() == null ? "" : student.getFullName().trim();
            int sp = fullName.indexOf(' ');
            body.put("firstName", sp > 0 ? fullName.substring(0, sp) : fullName);
            body.put("lastName", sp > 0 ? fullName.substring(sp + 1) : "(Estudiante)");
            body.put("emailAddress", student.getEmail());
            body.put("description", "Estudiante registrado desde Student Services (Grupo B)");
            body.put("cCursosCompletados", 0);

            Map<?, ?> resp = client.post()
                    .uri("/api/v1/Contact")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (resp != null && resp.get("id") != null) {
                log.info("[espocrm] contacto creado id={} email={}", resp.get("id"), student.getEmail());
                return resp.get("id").toString();
            }
            return null;
        } catch (Exception ex) {
            log.warn("[espocrm] createContact fallo para {}: {}", student.getEmail(), ex.getMessage());
            return null;
        }
    }

    /**
     * Incrementa cursos completados del contacto en EspoCRM.
     */
    @Async
    public void incrementCompletedCourses(Student student, String courseName) {
        if (!props.getEspocrm().isEnabled() || student.getEspoCrmContactId() == null) {
            log.debug("[espocrm] saltando incrementCompletedCourses (enabled={} contactId={})",
                    props.getEspocrm().isEnabled(), student.getEspoCrmContactId());
            return;
        }
        try {
            Map<String, Object> body = Map.of(
                    "cCursosCompletadosUltimo", courseName,
                    "description", "Completo curso: " + courseName
            );
            client.patch()
                    .uri("/api/v1/Contact/{id}", student.getEspoCrmContactId())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            log.info("[espocrm] contact {} actualizado con curso completado '{}'",
                    student.getEspoCrmContactId(), courseName);
        } catch (Exception ex) {
            log.warn("[espocrm] no se pudo actualizar contacto {}: {}",
                    student.getEspoCrmContactId(), ex.getMessage());
        }
    }
}
