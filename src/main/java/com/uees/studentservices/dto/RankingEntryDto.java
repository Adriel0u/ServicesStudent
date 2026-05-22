package com.uees.studentservices.dto;

import java.util.UUID;

public record RankingEntryDto(
        int position,
        UUID studentId,
        String studentName,
        String studentEmail,
        Integer progressPercent,
        boolean completed
) {}
