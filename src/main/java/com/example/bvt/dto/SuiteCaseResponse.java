package com.example.bvt.dto;

public record SuiteCaseResponse(
        Long id,
        Long suiteId,
        Long caseId,
        Long dataSetId,
        int execOrder,
        boolean enabled
) {
}
