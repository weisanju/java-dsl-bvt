package com.example.bvt.service;

import com.example.bvt.dto.CaseResultResponse;
import com.example.bvt.dto.RunReportResponse;
import com.example.bvt.dto.RunResponse;
import com.example.bvt.repository.TestCaseResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final RunService runService;
    private final TestCaseResultRepository caseResultRepository;

    public ReportService(RunService runService, TestCaseResultRepository caseResultRepository) {
        this.runService = runService;
        this.caseResultRepository = caseResultRepository;
    }

    public RunReportResponse getRunReport(Long runId) {
        RunResponse run = runService.getRun(runId);
        List<CaseResultResponse> results = caseResultRepository.findByRunIdOrderByIdAsc(runId).stream().map(entity ->
                new CaseResultResponse(
                        entity.getId(),
                        entity.getRun().getId(),
                        entity.getTestCase().getId(),
                        entity.getStepNo(),
                        entity.getStepName(),
                        entity.getStatus(),
                        entity.getResponseTimeMs(),
                        entity.getAssertionFailed(),
                        entity.getFailedAssertExpr(),
                        entity.getRequestSnapshot(),
                        entity.getResponseSnapshot(),
                        entity.getExtractedVars(),
                        entity.getErrorMessage()
                )).toList();
        return new RunReportResponse(run, results);
    }
}
