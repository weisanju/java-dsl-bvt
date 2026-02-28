package com.example.bvt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VariableUpsertRequest(
        @NotNull Long projectId,
        Long environmentId,
        @NotBlank @Size(max = 128) String varKey,
        @NotBlank String varValue
) {
}
