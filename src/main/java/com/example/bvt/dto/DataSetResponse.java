package com.example.bvt.dto;

import com.example.bvt.domain.enums.ConfigFormat;

public record DataSetResponse(
        Long id,
        Long caseId,
        String dataSetName,
        ConfigFormat dataFormat,
        String dataContent,
        boolean isDefault
) {
}
