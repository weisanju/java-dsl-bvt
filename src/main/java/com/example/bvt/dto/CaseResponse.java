package com.example.bvt.dto;

import com.example.bvt.domain.enums.CaseStatus;
import com.example.bvt.domain.enums.ConfigFormat;

public record CaseResponse(
        Long id,
        Long projectId,
        String caseCode,
        String caseName,
        ConfigFormat configFormat,
        String configContent,
        String normalizedDefinition,
        int versionNo,
        CaseStatus status
) {
}
