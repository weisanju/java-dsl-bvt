package com.example.bvt.controller;

import com.example.bvt.common.ApiResponse;
import com.example.bvt.dto.RunCreateRequest;
import com.example.bvt.dto.RunResponse;
import com.example.bvt.dto.SuiteBindCaseRequest;
import com.example.bvt.dto.SuiteCaseResponse;
import com.example.bvt.dto.SuiteCreateRequest;
import com.example.bvt.dto.SuiteResponse;
import com.example.bvt.service.RunService;
import com.example.bvt.service.SuiteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suites")
public class SuiteController {

    private final SuiteService suiteService;
    private final RunService runService;

    public SuiteController(SuiteService suiteService, RunService runService) {
        this.suiteService = suiteService;
        this.runService = runService;
    }

    @PostMapping
    public ApiResponse<SuiteResponse> create(@Valid @RequestBody SuiteCreateRequest request) {
        return ApiResponse.ok(suiteService.create(request));
    }

    @GetMapping
    public ApiResponse<List<SuiteResponse>> list(@RequestParam Long projectId) {
        return ApiResponse.ok(suiteService.list(projectId));
    }

    @PostMapping("/{id}/cases")
    public ApiResponse<SuiteCaseResponse> bindCase(@PathVariable Long id,
                                                   @Valid @RequestBody SuiteBindCaseRequest request) {
        return ApiResponse.ok(suiteService.bindCase(id, request));
    }

    @GetMapping("/{id}/cases")
    public ApiResponse<List<SuiteCaseResponse>> listSuiteCases(@PathVariable Long id) {
        return ApiResponse.ok(suiteService.listSuiteCases(id));
    }

    @PostMapping("/{id}/run")
    public ApiResponse<RunResponse> run(@PathVariable Long id, @Valid @RequestBody RunCreateRequest request) {
        return ApiResponse.ok(runService.triggerManual(id, request.environmentId()));
    }
}
