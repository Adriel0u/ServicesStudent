package com.uees.studentservices.service;

import com.uees.studentservices.dto.AuthResponse;
import com.uees.studentservices.dto.LoginRequest;
import com.uees.studentservices.dto.RefreshTokenRequest;
import com.uees.studentservices.dto.RegisterRequest;
import com.uees.studentservices.exception.DomainException;
import com.uees.studentservices.model.Role;
import com.uees.studentservices.model.Student;
import com.uees.studentservices.repository.StudentRepository;
import com.uees.studentservices.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final StudentRepository students;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final EspoCrmService espoCrm;
    private final EmailService email;

    public AuthService(StudentRepository students,
                       PasswordEncoder encoder,
                       AuthenticationManager authManager,
                       JwtService jwt,
                       EspoCrmService espoCrm,
                       EmailService email) {
        this.students = students;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
        this.espoCrm = espoCrm;
        this.email = email;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (students.existsByEmailIgnoreCase(req.email())) {
            throw DomainException.conflict("Ya existe un estudiante con ese email");
        }

        Student student = Student.builder()
                .email(req.email().toLowerCase())
                .fullName(req.fullName().trim())
                .profilePictureUrl(req.profilePictureUrl())
                .passwordHash(encoder.encode(req.password()))
                .role(Role.STUDENT)
                .active(true)
                .build();

        student = students.save(student);

        // EspoCRM (no bloquea el registro si falla)
        try {
            String crmId = espoCrm.createContact(student);
            if (crmId != null) {
                student.setEspoCrmContactId(crmId);
                student = students.save(student);
            }
        } catch (Exception ex) {
            log.warn("[auth] no se pudo crear contacto EspoCRM: {}", ex.getMessage());
        }

        // Email de bienvenida (async, no bloquea)
        email.sendWelcomeEmail(student.getEmail(), student.getFullName());

        return buildAuthResponse(student);
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (AuthenticationException ex) {
            throw new DomainException(org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Credenciales invalidas");
        }
        Student student = students.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> DomainException.notFound("Estudiante no encontrado"));
        return buildAuthResponse(student);
    }

    public AuthResponse refresh(RefreshTokenRequest req) {
        String token = req.refreshToken();
        if (!jwt.isTokenValid(token)) {
            throw new DomainException(org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Refresh token invalido o expirado");
        }
        if (!JwtService.TOKEN_TYPE_REFRESH.equals(jwt.extractTokenType(token))) {
            throw new DomainException(org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Token no es de tipo refresh");
        }
        Student student = students.findById(jwt.extractStudentId(token))
                .orElseThrow(() -> DomainException.notFound("Estudiante no encontrado"));
        return buildAuthResponse(student);
    }

    private AuthResponse buildAuthResponse(Student student) {
        String access = jwt.generateAccessToken(student);
        String refresh = jwt.generateRefreshToken(student);
        return new AuthResponse(
                access,
                refresh,
                "Bearer",
                jwt.getAccessTokenExpirationSeconds(),
                student.getId(),
                student.getEmail(),
                student.getFullName(),
                student.getProfilePictureUrl(),
                student.getRole().name()
        );
    }
}
