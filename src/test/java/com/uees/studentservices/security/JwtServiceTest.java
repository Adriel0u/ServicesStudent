package com.uees.studentservices.security;

import com.uees.studentservices.config.JwtProperties;
import com.uees.studentservices.model.Role;
import com.uees.studentservices.model.Student;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService buildService() {
        JwtProperties props = new JwtProperties();
        props.setSecret("eUVTRC1JTkdfV0VCXzIwMjZfU3VwZXJTZWNyZXRfS2V5X0F0X0xlYXN0XzMyX2J5dGVz");
        props.setAccessTokenExpirationMs(60_000);
        props.setRefreshTokenExpirationMs(120_000);
        return new JwtService(props);
    }

    private Student sampleStudent() {
        return Student.builder()
                .id(UUID.randomUUID())
                .email("test@uees.edu.sv")
                .fullName("Test Student")
                .passwordHash("hash")
                .role(Role.STUDENT)
                .active(true)
                .build();
    }

    @Test
    void access_token_is_valid_and_decodable() {
        JwtService svc = buildService();
        Student s = sampleStudent();

        String token = svc.generateAccessToken(s);

        assertThat(svc.isTokenValid(token)).isTrue();
        assertThat(svc.extractStudentId(token)).isEqualTo(s.getId());
        assertThat(svc.extractEmail(token)).isEqualTo(s.getEmail());
        assertThat(svc.extractRole(token)).isEqualTo("STUDENT");
        assertThat(svc.extractTokenType(token)).isEqualTo(JwtService.TOKEN_TYPE_ACCESS);
    }

    @Test
    void refresh_token_has_correct_type() {
        JwtService svc = buildService();
        String token = svc.generateRefreshToken(sampleStudent());

        assertThat(svc.isTokenValid(token)).isTrue();
        assertThat(svc.extractTokenType(token)).isEqualTo(JwtService.TOKEN_TYPE_REFRESH);
    }

    @Test
    void invalid_token_is_rejected() {
        JwtService svc = buildService();
        assertThat(svc.isTokenValid("not.a.valid.token")).isFalse();
    }
}
