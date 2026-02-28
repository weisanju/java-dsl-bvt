package com.example.bvt.engine;

import com.example.bvt.common.BusinessException;
import com.example.bvt.engine.model.HttpResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExtractEngine {

    private final QlExpressEngine qlExpressEngine;
    private final ObjectMapper objectMapper;

    public ExtractEngine(QlExpressEngine qlExpressEngine, ObjectMapper objectMapper) {
        this.qlExpressEngine = qlExpressEngine;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> extract(Map<String, String> extracts,
                                       Map<String, Object> context,
                                       HttpResponseData responseData) {
        Map<String, Object> extracted = new HashMap<>();
        if (extracts == null || extracts.isEmpty()) {
            return extracted;
        }
        Map<String, Object> evalContext = new HashMap<>(context);
        evalContext.put("status", responseData.status());
        evalContext.put("headers", responseData.headers());
        evalContext.put("body", parseBody(responseData.body()));
        for (Map.Entry<String, String> entry : extracts.entrySet()) {
            try {
                Object value = qlExpressEngine.execute(entry.getValue(), evalContext);
                extracted.put(entry.getKey(), value);
                context.put(entry.getKey(), value);
                evalContext.put(entry.getKey(), value);
            } catch (Exception ex) {
                throw new BusinessException("Extract variable failed: " + entry.getKey() + ", " + ex.getMessage());
            }
        }
        return extracted;
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
