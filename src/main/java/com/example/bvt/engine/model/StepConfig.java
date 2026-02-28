package com.example.bvt.engine.model;

import java.util.List;
import java.util.Map;

public record StepConfig(
        String name,
        RequestConfig request,
        List<String> assertions,
        Map<String, String> extracts,
        String skipWhen
) {
}
