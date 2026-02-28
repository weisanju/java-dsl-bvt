package com.example.bvt.dto;

import jakarta.validation.constraints.NotNull;

public record PlanEnableRequest(@NotNull Boolean enabled) {
}
