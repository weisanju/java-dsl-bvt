package com.example.bvt.controller;

import com.example.bvt.common.ApiResponse;
import com.example.bvt.dto.EnvironmentCreateRequest;
import com.example.bvt.dto.EnvironmentResponse;
import com.example.bvt.service.EnvironmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/environments")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @PostMapping
    public ApiResponse<EnvironmentResponse> create(@Valid @RequestBody EnvironmentCreateRequest request) {
        return ApiResponse.ok(environmentService.create(request));
    }

    @GetMapping
    public ApiResponse<List<EnvironmentResponse>> list(@RequestParam Long projectId) {
        return ApiResponse.ok(environmentService.list(projectId));
    }
}
