package com.example.bvt.controller;

import com.example.bvt.common.ApiResponse;
import com.example.bvt.dto.PlanCreateRequest;
import com.example.bvt.dto.PlanEnableRequest;
import com.example.bvt.dto.PlanResponse;
import com.example.bvt.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ApiResponse<PlanResponse> create(@Valid @RequestBody PlanCreateRequest request) {
        return ApiResponse.ok(planService.create(request));
    }

    @PatchMapping("/{id}/enable")
    public ApiResponse<PlanResponse> enable(@PathVariable Long id, @Valid @RequestBody PlanEnableRequest request) {
        return ApiResponse.ok(planService.enable(id, request));
    }

    @GetMapping
    public ApiResponse<List<PlanResponse>> list() {
        return ApiResponse.ok(planService.list());
    }
}
