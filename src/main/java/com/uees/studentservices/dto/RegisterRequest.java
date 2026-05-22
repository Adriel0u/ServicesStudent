package com.uees.studentservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 3, max = 180) String fullName,
        @NotBlank @Size(min = 8, max = 100) String password,
        @Size(max = 500) String profilePictureUrl
) {}
