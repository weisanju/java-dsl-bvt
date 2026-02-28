package com.example.bvt.dto;

import com.example.bvt.domain.enums.PlanStatus;

import java.time.Instant;

public record PlanResponse(
        Long id,
        Long projectId,
        String planName,
        Long suiteId,
        Long environmentId,
        String cronExpr,
        PlanStatus status,
        Instant lastRunAt
) {
}
