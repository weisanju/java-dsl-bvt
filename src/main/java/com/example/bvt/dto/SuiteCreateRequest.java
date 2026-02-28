package com.example.bvt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SuiteCreateRequest(
        @NotNull Long projectId,
        @NotBlank @Size(max = 64) String suiteCode,
        @NotBlank @Size(max = 128) String suiteName,
        @Size(max = 1024) String description
) {
}
