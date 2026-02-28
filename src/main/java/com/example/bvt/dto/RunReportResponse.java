package com.example.bvt.dto;

import java.util.List;

public record RunReportResponse(RunResponse run, List<CaseResultResponse> results) {
}
