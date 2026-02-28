package com.example.bvt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlanCreateRequest(
        @NotNull Long projectId,
        @NotBlank @Size(max = 128) String planName,
        @NotNull Long suiteId,
        @NotNull Long environmentId,
        @NotBlank @Size(max = 128) String cronExpr
) {
}
