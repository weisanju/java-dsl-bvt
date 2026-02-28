package com.example.bvt.controller;

import com.example.bvt.common.ApiResponse;
import com.example.bvt.dto.VariableResponse;
import com.example.bvt.dto.VariableUpsertRequest;
import com.example.bvt.service.VariableService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/variables")
public class VariableController {

    private final VariableService variableService;

    public VariableController(VariableService variableService) {
        this.variableService = variableService;
    }

    @PostMapping
    public ApiResponse<VariableResponse> upsert(@Valid @RequestBody VariableUpsertRequest request) {
        return ApiResponse.ok(variableService.upsert(request));
    }

    @GetMapping
    public ApiResponse<List<VariableResponse>> list(@RequestParam Long projectId,
                                                    @RequestParam(required = false) Long environmentId) {
        return ApiResponse.ok(variableService.list(projectId, environmentId));
    }
}
