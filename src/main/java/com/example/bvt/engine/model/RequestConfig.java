package com.example.bvt.engine.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public record RequestConfig(
        String method,
        String urlExpr,
        Map<String, String> headersExpr,
        JsonNode bodyExpr,
        Integer timeoutMs
) {
}
