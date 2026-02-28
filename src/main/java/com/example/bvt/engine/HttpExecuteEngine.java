package com.example.bvt.engine;

import com.example.bvt.common.BusinessException;
import com.example.bvt.engine.model.HttpResponseData;
import com.example.bvt.engine.model.RenderedRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpExecuteEngine {

    private final HttpClient httpClient;

    public HttpExecuteEngine(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponseData execute(RenderedRequest request) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(request.url()))
                    .timeout(request.timeout());
            request.headers().forEach(builder::header);
            String method = request.method();
            String body = request.body();
            if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else if (body == null) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(body));
            }
            Instant start = Instant.now();
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            long elapsed = java.time.Duration.between(start, Instant.now()).toMillis();
            Map<String, String> headers = new HashMap<>();
            response.headers().map().forEach((key, values) -> headers.put(key, values.isEmpty() ? "" : values.getFirst()));
            return new HttpResponseData(response.statusCode(), headers, response.body(), elapsed);
        } catch (Exception ex) {
            throw new BusinessException("HTTP execute failed: " + ex.getMessage());
        }
    }
}
