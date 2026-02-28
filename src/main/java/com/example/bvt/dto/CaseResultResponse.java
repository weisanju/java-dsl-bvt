package com.example.bvt.dto;

import com.example.bvt.domain.enums.ResultStatus;

public record CaseResultResponse(
        Long id,
        Long runId,
        Long caseId,
        Integer stepNo,
        String stepName,
        ResultStatus status,
        Integer responseTimeMs,
        int assertionFailed,
        String failedAssertExpr,
        String requestSnapshot,
        String responseSnapshot,
        String extractedVars,
        String errorMessage
) {
}
