package com.example.bvt.controller;

import com.example.bvt.common.ApiResponse;
import com.example.bvt.dto.RunReportResponse;
import com.example.bvt.dto.RunResponse;
import com.example.bvt.service.ReportService;
import com.example.bvt.service.RunService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final RunService runService;
    private final ReportService reportService;

    public RunController(RunService runService, ReportService reportService) {
        this.runService = runService;
        this.reportService = reportService;
    }

    @GetMapping("/{id}")
    public ApiResponse<RunResponse> getRun(@PathVariable Long id) {
        return ApiResponse.ok(runService.getRun(id));
    }

    @GetMapping("/{id}/report")
    public ApiResponse<RunReportResponse> getReport(@PathVariable Long id) {
        return ApiResponse.ok(reportService.getRunReport(id));
    }
}
