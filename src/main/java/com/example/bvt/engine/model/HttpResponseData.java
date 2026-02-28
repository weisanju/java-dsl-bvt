package com.example.bvt.engine.model;

import java.util.Map;

public record HttpResponseData(int status, Map<String, String> headers, String body, long elapsedMs) {
}
