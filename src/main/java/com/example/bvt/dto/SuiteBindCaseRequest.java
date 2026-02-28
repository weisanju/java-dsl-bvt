package com.example.bvt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SuiteBindCaseRequest(
        @NotNull Long caseId,
        Long dataSetId,
        @PositiveOrZero int execOrder,
        boolean enabled
) {
}
