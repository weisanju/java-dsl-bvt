package com.example.bvt.dto;

public record VariableResponse(
        Long id,
        Long projectId,
        Long environmentId,
        String varKey,
        String varValue
) {
}
