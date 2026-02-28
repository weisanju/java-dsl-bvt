package com.example.bvt.engine;

import com.example.bvt.engine.model.AssertionResult;
import com.example.bvt.engine.model.HttpResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AssertEngine {

    private final QlExpressEngine qlExpressEngine;
    private final ObjectMapper objectMapper;

    public AssertEngine(QlExpressEngine qlExpressEngine, ObjectMapper objectMapper) {
        this.qlExpressEngine = qlExpressEngine;
        this.objectMapper = objectMapper;
    }

    public AssertionResult evaluate(List<String> assertions, Map<String, Object> context, HttpResponseData responseData) {
        if (assertions == null || assertions.isEmpty()) {
            return AssertionResult.pass();
        }
        Map<String, Object> evalContext = buildEvalContext(context, responseData);
        for (String expression : assertions) {
            try {
                boolean passed = qlExpressEngine.evalBoolean(expression, evalContext);
                if (!passed) {
                    return AssertionResult.fail(expression, "Assertion evaluate result=false");
                }
            } catch (Exception ex) {
                return AssertionResult.fail(expression, ex.getMessage());
            }
        }
        return AssertionResult.pass();
    }

    private Map<String, Object> buildEvalContext(Map<String, Object> context, HttpResponseData responseData) {
        Map<String, Object> evalContext = new HashMap<>(context);
        evalContext.put("status", responseData.status());
        evalContext.put("headers", responseData.headers());
        evalContext.put("body", parseBody(responseData.body()));
        return evalContext;
    }

    private Object parseBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String trimmed = body.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                return objectMapper.readValue(trimmed, Object.class);
            } catch (Exception ignored) {
                return trimmed;
            }
        }
        return trimmed;
    }
}
