package com.example.bvt.engine.model;

import java.util.List;

public record CaseConfig(Meta meta, List<StepConfig> steps) {

    public record Meta(String caseCode, String caseName, Integer version, List<String> tags) {
    }
}
