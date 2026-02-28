package com.example.bvt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnvironmentCreateRequest(
        @NotNull Long projectId,
        @NotBlank @Size(max = 32) String envCode,
        @NotBlank @Size(max = 64) String envName,
        @NotBlank @Size(max = 512) String baseUrl,
        boolean isDefault
) {
}
