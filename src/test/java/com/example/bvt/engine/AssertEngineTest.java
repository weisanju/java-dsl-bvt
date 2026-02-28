package com.example.bvt.engine;

import com.example.bvt.engine.model.HttpResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class AssertEngineTest {

    private final QlExpressEngine qlExpressEngine = new QlExpressEngine(new ExpressionSecurityValidator());
    private final AssertEngine assertEngine = new AssertEngine(qlExpressEngine, new ObjectMapper());

    @Test
    void shouldPassWhenAssertionTrue() {
        HttpResponseData response = new HttpResponseData(200, Map.of(), "{\"code\":0}", 10);
        var result = assertEngine.evaluate(List.of("status == 200", "body.code == 0"), Map.of(), response);
        Assertions.assertTrue(result.passed());
    }

    @Test
    void shouldFailWhenAssertionFalse() {
        HttpResponseData response = new HttpResponseData(500, Map.of(), "{\"code\":1}", 10);
        var result = assertEngine.evaluate(List.of("status == 200"), Map.of(), response);
        Assertions.assertFalse(result.passed());
        Assertions.assertEquals("status == 200", result.failedExpression());
    }
}
