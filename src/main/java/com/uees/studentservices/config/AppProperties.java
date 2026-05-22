package com.uees.studentservices.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Rabbit rabbit = new Rabbit();
    private final Mail mail = new Mail();
    private final EspoCrm espocrm = new EspoCrm();
    private final LearningEngine learningEngine = new LearningEngine();
    private final Cors cors = new Cors();

    public Rabbit getRabbit() { return rabbit; }
    public Mail getMail() { return mail; }
    public EspoCrm getEspocrm() { return espocrm; }
    public LearningEngine getLearningEngine() { return learningEngine; }
    public Cors getCors() { return cors; }

    public static class Rabbit {
        private String enrollmentsExchange = "enrollments.exchange";
        private String enrollmentActivatedQueue = "student.enrollment.activated.q";
        private String enrollmentActivatedRoutingKey = "enrollment.activated";
        private String moduleCompletedQueue = "student.module.completed.q";
        private String moduleCompletedRoutingKey = "module.completed";
        private String deadLetterExchange = "enrollments.dlx";
        private String deadLetterRoutingKey = "enrollments.dlq";

        public String getEnrollmentsExchange() { return enrollmentsExchange; }
        public void setEnrollmentsExchange(String v) { this.enrollmentsExchange = v; }

        public String getEnrollmentActivatedQueue() { return enrollmentActivatedQueue; }
        public void setEnrollmentActivatedQueue(String v) { this.enrollmentActivatedQueue = v; }

        public String getEnrollmentActivatedRoutingKey() { return enrollmentActivatedRoutingKey; }
        public void setEnrollmentActivatedRoutingKey(String v) { this.enrollmentActivatedRoutingKey = v; }

        public String getModuleCompletedQueue() { return moduleCompletedQueue; }
        public void setModuleCompletedQueue(String v) { this.moduleCompletedQueue = v; }

        public String getModuleCompletedRoutingKey() { return moduleCompletedRoutingKey; }
        public void setModuleCompletedRoutingKey(String v) { this.moduleCompletedRoutingKey = v; }

        public String getDeadLetterExchange() { return deadLetterExchange; }
        public void setDeadLetterExchange(String v) { this.deadLetterExchange = v; }

        public String getDeadLetterRoutingKey() { return deadLetterRoutingKey; }
        public void setDeadLetterRoutingKey(String v) { this.deadLetterRoutingKey = v; }
    }

    public static class Mail {
        private String from = "no-reply@uees-lms.local";
        private String fromName = "UEES Plataforma de Cursos";

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }

        public String getFromName() { return fromName; }
        public void setFromName(String fromName) { this.fromName = fromName; }
    }

    public static class EspoCrm {
        private String baseUrl = "http://localhost:8082";
        private String apiKey = "changeme";
        private boolean enabled = false;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class LearningEngine {
        private String baseUrl = "http://localhost:8080";
        private String apiPrefix = "/api";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getApiPrefix() { return apiPrefix; }
        public void setApiPrefix(String apiPrefix) { this.apiPrefix = apiPrefix; }
    }

    public static class Cors {
        private String allowedOrigins = "*";

        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }
}
