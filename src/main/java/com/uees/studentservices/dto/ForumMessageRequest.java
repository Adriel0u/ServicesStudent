package com.uees.studentservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForumMessageRequest(
        @NotBlank @Size(max = 2000) String content
) {}
