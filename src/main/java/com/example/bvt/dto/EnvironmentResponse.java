package com.example.bvt.dto;

public record EnvironmentResponse(
        Long id,
        Long projectId,
        String envCode,
        String envName,
        String baseUrl,
        boolean isDefault
) {
}
