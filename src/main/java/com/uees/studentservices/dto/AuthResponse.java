package com.uees.studentservices.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UUID studentId,
        String email,
        String fullName,
        String profilePictureUrl,
        String role
) {}
