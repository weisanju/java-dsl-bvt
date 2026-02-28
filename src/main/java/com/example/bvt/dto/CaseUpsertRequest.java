package com.example.bvt.dto;

import com.example.bvt.domain.enums.ConfigFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CaseUpsertRequest(
        @NotNull Long projectId,
        @NotBlank @Size(max = 64) String caseCode,
        @NotBlank @Size(max = 128) String caseName,
        @NotNull ConfigFormat configFormat,
        @NotBlank String configContent,
        String tags
) {
}
