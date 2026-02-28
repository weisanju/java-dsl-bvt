package com.example.bvt.dto;

import com.example.bvt.domain.enums.RunStatus;
import com.example.bvt.domain.enums.TriggerType;

import java.time.Instant;

public record RunResponse(
        Long id,
        Long projectId,
        Long suiteId,
        Long environmentId,
        TriggerType triggerType,
        RunStatus status,
        int totalCases,
        int passedCases,
        int failedCases,
        int skippedCases,
        Instant startedAt,
        Instant finishedAt,
        Long durationMs,
        String errorSummary
) {
}
