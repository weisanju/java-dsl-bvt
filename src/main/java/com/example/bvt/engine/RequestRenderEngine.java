package com.example.bvt.engine;

import com.example.bvt.common.BusinessException;
import com.example.bvt.config.ExecutionProperties;
import com.example.bvt.engine.model.RenderedRequest;
import com.example.bvt.engine.model.RequestConfig;
import com.example.bvt.engine.model.StepConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class RequestRenderEngine {

    private final QlExpressEngine qlExpressEngine;
    private final ObjectMapper objectMapper;
    private final ExecutionProperties executionProperties;

    public RequestRenderEngine(QlExpressEngine qlExpressEngine,
                               ObjectMapper objectMapper,
                               ExecutionProperties executionProperties) {
        this.qlExpressEngine = qlExpressEngine;
        this.objectMapper = objectMapper;
        this.executionProperties = executionProperties;
    }

    public RenderedRequest render(StepConfig step, Map<String, Object> context) {
        RequestConfig request = step.request();
        if (request == null) {
            throw new BusinessException("Step request cannot be null");
        }
        String method = request.method() == null ? "GET" : request.method().trim().toUpperCase(Locale.ROOT);
        String url = String.valueOf(qlExpressEngine.execute(request.urlExpr(), context));
        Map<String, String> headers = renderHeaders(request.headersExpr(), context);
        String body = renderBody(request.bodyExpr(), context);
        int timeoutMs = request.timeoutMs() != null ? request.timeoutMs() : executionProperties.getStepTimeoutMs();
        return new RenderedRequest(method, url, headers, body, Duration.ofMillis(timeoutMs));
    }

    private Map<String, String> renderHeaders(Map<String, String> headerExpr, Map<String, Object> context) {
        Map<String, String> headers = new HashMap<>();
        if (headerExpr == null || headerExpr.isEmpty()) {
            return headers;
        }
        for (Map.Entry<String, String> entry : headerExpr.entrySet()) {
            Object value = qlExpressEngine.execute(entry.getValue(), context);
            headers.put(entry.getKey(), value == null ? "" : String.valueOf(value));
        }
        return headers;
    }

    private String renderBody(JsonNode bodyExpr, Map<String, Object> context) {
        if (bodyExpr == null || bodyExpr.isNull()) {
            return null;
        }
        try {
            if (bodyExpr.isTextual()) {
                Object eval = qlExpressEngine.execute(bodyExpr.asText(), context);
                if (eval == null) {
                    return null;
                }
                if (eval instanceof String str) {
                    return str;
                }
                return objectMapper.writeValueAsString(eval);
            }
            return objectMapper.writeValueAsString(bodyExpr);
        } catch (Exception ex) {
            throw new BusinessException("Render request body failed: " + ex.getMessage());
        }
    }
}
