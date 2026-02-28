package com.example.bvt.dto;

import com.example.bvt.domain.enums.ConfigFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DataSetCreateRequest(
        @NotBlank @Size(max = 128) String dataSetName,
        @NotNull ConfigFormat dataFormat,
        @NotBlank String dataContent,
        boolean isDefault
) {
}
