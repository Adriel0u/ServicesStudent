package com.uees.studentservices.controller;

import com.uees.studentservices.dto.AuthResponse;
import com.uees.studentservices.dto.LoginRequest;
import com.uees.studentservices.dto.RefreshTokenRequest;
import com.uees.studentservices.dto.RegisterRequest;
import com.uees.studentservices.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registro y login JWT de estudiantes")
@SecurityRequirements
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @Operation(summary = "Registrar estudiante (crea contacto en EspoCRM)")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(auth.register(req));
    }

    @Operation(summary = "Login JWT (devuelve accessToken + refreshToken)")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(auth.login(req));
    }

    @Operation(summary = "Renovar access token a partir del refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(auth.refresh(req));
    }
}
