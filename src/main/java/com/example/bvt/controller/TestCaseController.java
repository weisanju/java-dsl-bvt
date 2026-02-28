package com.example.bvt.controller;

import com.example.bvt.common.ApiResponse;
import com.example.bvt.dto.CaseResponse;
import com.example.bvt.dto.CaseUpsertRequest;
import com.example.bvt.dto.DataSetCreateRequest;
import com.example.bvt.dto.DataSetResponse;
import com.example.bvt.service.DataSetService;
import com.example.bvt.service.TestCaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
public class TestCaseController {

    private final TestCaseService testCaseService;
    private final DataSetService dataSetService;

    public TestCaseController(TestCaseService testCaseService, DataSetService dataSetService) {
        this.testCaseService = testCaseService;
        this.dataSetService = dataSetService;
    }

    @PostMapping
    public ApiResponse<CaseResponse> create(@Valid @RequestBody CaseUpsertRequest request) {
        return ApiResponse.ok(testCaseService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CaseResponse> update(@PathVariable Long id, @Valid @RequestBody CaseUpsertRequest request) {
        return ApiResponse.ok(testCaseService.update(id, request));
    }

    @GetMapping
    public ApiResponse<List<CaseResponse>> list(@RequestParam Long projectId) {
        return ApiResponse.ok(testCaseService.list(projectId));
    }

    @PostMapping("/{id}/datasets")
    public ApiResponse<DataSetResponse> createDataSet(@PathVariable Long id,
                                                      @Valid @RequestBody DataSetCreateRequest request) {
        return ApiResponse.ok(dataSetService.create(id, request));
    }

    @GetMapping("/{id}/datasets")
    public ApiResponse<List<DataSetResponse>> listDataSet(@PathVariable Long id) {
        return ApiResponse.ok(dataSetService.list(id));
    }
}
