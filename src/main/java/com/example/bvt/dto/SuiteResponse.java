package com.example.bvt.dto;

import com.example.bvt.domain.enums.SuiteStatus;

public record SuiteResponse(
        Long id,
        Long projectId,
        String suiteCode,
        String suiteName,
        String description,
        SuiteStatus status
) {
}
