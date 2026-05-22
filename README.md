# Student Services — Grupo B  
**Plataforma de Cursos en Línea (LMS) · Ingeniería de la Web 2026 · UEES**

Microservicio del **Grupo B — Student Services** del proyecto integrador. Acompaña al estudiante a lo largo de toda su experiencia en la plataforma: registro seguro con JWT, seguimiento de progreso académico, foro en tiempo real, certificados por email y sincronización con EspoCRM.

Está diseñado para integrarse con el **Grupo A — Learning Engine** vía:

- **RabbitMQ Consumer** de los eventos `enrollment.activated` y `module.completed`.
- **WebClient** REST contra el catálogo de cursos y verificación de inscripciones COMPLETED.

---

## Tabla de contenidos

1. [Stack tecnológico](#stack-tecnológico)
2. [Módulos implementados](#módulos-implementados)
3. [Endpoints REST](#endpoints-rest)
4. [Contrato de mensajes RabbitMQ](#contrato-de-mensajes-rabbitmq)
5. [Cómo ejecutarlo](#cómo-ejecutarlo)
6. [Docker Compose](#docker-compose)
7. [Variables de entorno](#variables-de-entorno)
8. [Cómo probar manualmente](#cómo-probar-manualmente)
9. [Estructura del proyecto](#estructura-del-proyecto)
10. [Mapeo con la rúbrica del PDF](#mapeo-con-la-rúbrica-del-pdf)

---

## Stack tecnológico

- **Java 17 + Spring Boot 3.3.4**
- **Spring Security + JWT (jjwt 0.12.6)** con refresh token
- **Spring Data JPA + MySQL 8** (perfil `local` usa H2 en memoria)
- **Spring AMQP / RabbitMQ** (Consumer + DLX para mensajes inválidos)
- **Spring WebSocket STOMP** + SockJS para el foro en tiempo real
- **Spring Mail + Thymeleaf** para el certificado HTML
- **Spring WebFlux WebClient** hacia el Learning Engine (Grupo A) y EspoCRM
- **SpringDoc OpenAPI 2.6** → Swagger UI documentado al 100%
- **Docker + Docker Compose**

---

## Módulos implementados

### 1. Registro y Perfil de Estudiante
- `POST /api/auth/register` → email único, nombre completo, foto de perfil opcional.
- `POST /api/auth/login` → JWT access + refresh.
- `POST /api/auth/refresh` → renovación del access token.
- Al registrar: se crea automáticamente el **Contact en EspoCRM** vía WebClient y se envía email de bienvenida.

### 2. Seguimiento de Progreso
- Consumer RabbitMQ sobre el exchange compartido `enrollments.exchange`.
- Cuando llega `enrollment.activated`: se crea el espejo local con 0% de progreso y se envía email de bienvenida del curso.
- Cuando llega `module.completed`: se incrementa el contador, se recalcula el % (idempotente por `enrollmentId+moduleId`), y al llegar a **100%** se emite y envía el certificado por email.
- `GET /api/progress/{courseId}` → estado actual del estudiante autenticado.
- `GET /api/progress/{courseId}/ranking?page=0&size=20` → **ranking Pageable** por % de progreso.

### 3. Foro de Discusión (WebSocket)
- Endpoint STOMP: `/ws-forum` (SockJS) y `/ws` (nativo).
- Tópico por curso: `/topic/course/{courseId}/forum`.
- Publicar por REST (`POST /api/courses/{id}/forum`) o por STOMP (`SEND /app/course/{id}/forum`).
- Persistencia en BD para historial.
- `GET /api/courses/{id}/forum?page=0&size=20` → historial paginado.
- Página de demo en `http://localhost:8081/forum-demo.html` (envía/recibe en vivo).

### 4. Certificados
- Template **Thymeleaf** con diseño profesional (`certificate.html`).
- Envío automático al alcanzar 100% (consumer `module.completed`).
- `GET /api/certificates/{studentId}` → listado del estudiante.
- `GET /api/certificates/me` → del autenticado.
- Verificación previa con **Grupo A** (`GET /api/enrollments/{id}` debe estar `COMPLETED`).
- Al emitir: se actualiza EspoCRM con el campo `cursos completados`.

### 5. Dashboard personal
- `GET /api/dashboard` → resumen completo: cursos inscritos + progreso + certificados + **catálogo disponible** consultado al Grupo A vía WebClient.

---

## Endpoints REST

Todos los endpoints están documentados en **Swagger UI** → http://localhost:8081/swagger-ui.html

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Registro JWT + EspoCRM | público |
| POST | `/api/auth/login` | Login (access + refresh) | público |
| POST | `/api/auth/refresh` | Renovar access token | público |
| GET  | `/api/dashboard` | Dashboard del estudiante | JWT |
| GET  | `/api/progress/{courseId}` | Progreso en un curso | JWT |
| GET  | `/api/progress/{courseId}/ranking` | Ranking Pageable | JWT |
| GET  | `/api/courses/{id}/forum` | Historial foro paginado | JWT |
| POST | `/api/courses/{id}/forum` | Publicar mensaje (también vía WS) | JWT |
| GET  | `/api/certificates/{studentId}` | Certificados del estudiante | JWT |
| GET  | `/api/certificates/me` | Mis certificados | JWT |
| POST | `/api/crm/sync` | Sincronización manual con EspoCRM | JWT |

---

## Contrato de mensajes RabbitMQ

> Acordado con el Grupo A (sección "Contrato compartido" del PDF).

**Exchange:** `enrollments.exchange` *(tipo `topic`, durable)*

### `enrollment.activated`
```json
{
  "enrollmentId": "UUID",
  "studentId":    "UUID",
  "studentEmail": "string",
  "studentName":  "string",
  "courseId":     "UUID",
  "courseName":   "string",
  "activatedAt":  "2026-05-19T20:00:00Z"
}
```

### `module.completed`
```json
{
  "enrollmentId":      "UUID",
  "moduleId":          "UUID",
  "moduleName":        "string",
  "completionPercent": 100
}
```

**Colas creadas por este servicio (con DLX `enrollments.dlx` → `enrollments.dlq`):**

- `student.enrollment.activated.q` ← `enrollment.activated`
- `student.module.completed.q` ← `module.completed`

---

## Cómo ejecutarlo

### Opción A — Local con perfil `local` (H2 en memoria, sin Docker)

```bash
mvn spring-boot:run
# Swagger:        http://localhost:8081/swagger-ui.html
# H2 console:     http://localhost:8081/h2-console  (jdbc:h2:mem:student_db, user: sa, pass: vacío)
# Foro demo:      http://localhost:8081/forum-demo.html
```

> Nota: en modo `local` no hay RabbitMQ corriendo, así que los eventos no llegan. Para probar el flujo completo usa Docker.

### Opción B — Todo con Docker Compose

```bash
cp .env.example .env       # edita credenciales de Mailtrap
docker compose up --build
```

Esto levanta MySQL, RabbitMQ, EspoCRM (opcional) y el servicio en `http://localhost:8081`.

### Opción C — Integración con Grupo A

Ambos `docker-compose.yml` deben unirse a la red **`lms-net`** (declarada con `name: lms-net`). El servicio Group A debe llamarse `learning-engine` en su compose para que la URL `http://learning-engine:8080` resuelva.

---

## Docker Compose

| Servicio | Imagen | Puerto host | Responsable PDF |
|---|---|---|---|
| `mysql-student` | mysql:8.0 | 3307 | Grupo B |
| `rabbitmq` | rabbitmq:3-management | 5672 / 15672 | Compartido |
| `espocrm` (+ `espocrm-db`) | espocrm/espocrm | 8082 | Grupo B |
| `student-services` | build local | 8081 | Grupo B |

RabbitMQ management UI: `http://localhost:15672` (`guest` / `guest`).

---

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | `local` (H2) o `docker` (MySQL+Rabbit) |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | ... | MySQL `mysql-student` |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | Broker compartido |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | Mailtrap sandbox | SMTP |
| `ESPOCRM_BASE_URL` / `ESPOCRM_API_KEY` / `ESPOCRM_ENABLED` | — | CRM Contacts |
| `LEARNING_ENGINE_URL` | `http://localhost:8080` | WebClient Grupo A |
| `JWT_SECRET` | base64 32+ bytes | firma JWT |

---

## Cómo probar manualmente

### 1) Smoke vía REST Client

Abre `docs/requests.http` en IntelliJ / VS Code (REST Client) y ejecuta los 12 requests en orden.

### 2) Simular el evento del Grupo A desde RabbitMQ UI

`http://localhost:15672` → **Exchanges** → `enrollments.exchange` → **Publish message**

- routing key: `enrollment.activated`
- payload (JSON):
```json
{
  "enrollmentId": "11111111-1111-1111-1111-111111111111",
  "studentId":    "<pega aqui el id devuelto en login>",
  "studentEmail": "ana.lopez@uees.edu.sv",
  "studentName":  "Ana Lopez",
  "courseId":     "22222222-2222-2222-2222-222222222222",
  "courseName":   "Ingenieria de la Web",
  "activatedAt":  "2026-05-19T20:00:00Z"
}
```

Después publica varios `module.completed` aumentando `completionPercent` hasta 100, y verifica:
- el progreso vía `GET /api/progress/22222222-...`,
- el certificado en `GET /api/certificates/me`,
- el correo en tu inbox de **Mailtrap**.

### 3) Foro en vivo

Abre `http://localhost:8081/forum-demo.html`, pega tu access token y empieza a chatear con otro navegador apuntado al mismo `courseId`.

---

## Estructura del proyecto

```
SERVICESTUDENT/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── docs/
│   └── requests.http
├── src/main/java/com/uees/studentservices/
│   ├── StudentServicesApplication.java
│   ├── config/        # Security, JWT, RabbitMQ, WebSocket, WebClient, OpenAPI, properties
│   ├── controller/    # AuthController, DashboardController, ProgressController,
│   │                  # ForumController, ForumStompController, CertificateController, CrmController
│   ├── dto/           # Records de request/response + dto/events (eventos Rabbit)
│   ├── exception/     # DomainException + GlobalExceptionHandler
│   ├── messaging/     # EnrollmentEventListener (consumers Rabbit)
│   ├── model/         # Entidades JPA (Student, EnrollmentProgress, ModuleCompletion,
│   │                  # ForumMessage, Certificate, Role)
│   ├── repository/    # Spring Data JPA repositories
│   ├── security/      # JwtService, JwtAuthenticationFilter, StudentPrincipal,
│   │                  # StudentUserDetailsService, RestAuthEntryPoint, AuthUtils
│   └── service/       # AuthService, ProgressService, CertificateService, ForumService,
│                      # DashboardService, EmailService, EspoCrmService, LearningEngineClient
└── src/main/resources/
    ├── application.yml      # multiperfil: default / local (H2) / docker (MySQL+Rabbit)
    ├── templates/           # Plantillas Thymeleaf (certificate, welcome, enrollment-welcome)
    └── static/forum-demo.html
```

---

## Mapeo con la rúbrica del PDF

| Requisito del PDF (Grupo B) | Ubicación |
|---|---|
| Registro con email único, nombre, foto URL | `RegisterRequest`, `AuthService.register` |
| Login JWT con refreshToken | `AuthService.login` / `refresh`, `JwtService` |
| Crear contacto en EspoCRM al registrar | `EspoCrmService.createContact` |
| Dashboard personal (inscritos + progreso + certs) | `DashboardController`, `DashboardService` |
| Consumer eventos `module.completed` | `EnrollmentEventListener.onModuleCompleted` |
| Cálculo % de avance por curso | `ProgressService.applyModuleCompleted` |
| Emitir certificado vía Spring Mail + Thymeleaf | `CertificateService.issueAndSend` + `certificate.html` |
| Ranking de estudiantes por progreso (Pageable) | `ProgressService.rankingByCourse` |
| WebSocket STOMP `/topic/course/{id}/forum` | `WebSocketConfig`, `ForumService`, `ForumStompController` |
| Foro persistido en BD | `ForumMessage`, `ForumMessageRepository` |
| Historial paginado | `ForumController.history` |
| Template Thymeleaf de certificado | `templates/certificate.html` |
| Endpoint listado certificados `/api/certificates/{studentId}` | `CertificateController` |
| Actualizar EspoCRM con 'cursos completados' | `EspoCrmService.incrementCompletedCourses` |
| WebClient al catálogo Grupo A | `LearningEngineClient.fetchCourses` |
| Verificación COMPLETED Grupo A | `LearningEngineClient.isEnrollmentCompleted` |
| Swagger documentado | `OpenApiConfig` + anotaciones |
| Docker Compose completo | `docker-compose.yml` |
| Contrato RabbitMQ compartido | `EnrollmentActivatedEvent`, `ModuleCompletedEvent`, `RabbitMQConfig` |

---

## Autor

Universidad Evangélica de El Salvador — Ingeniería de la Web 2026  
Proyecto integrador con el Grupo A (Learning Engine).
