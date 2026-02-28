package com.example.bvt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
        @NotBlank @Size(max = 64) String projectKey,
        @NotBlank @Size(max = 128) String projectName,
        @Size(max = 1024) String description
) {
}
