package com.example.bvt.engine.model;

import java.time.Duration;
import java.util.Map;

public record RenderedRequest(
        String method,
        String url,
        Map<String, String> headers,
        String body,
        Duration timeout
) {
}
