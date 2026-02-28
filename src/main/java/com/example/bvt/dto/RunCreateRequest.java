package com.example.bvt.dto;

import jakarta.validation.constraints.NotNull;

public record RunCreateRequest(@NotNull Long environmentId) {
}
