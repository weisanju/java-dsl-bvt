package com.example.bvt.engine.model;

public record AssertionResult(boolean passed, String failedExpression, String errorMessage) {

    public static AssertionResult pass() {
        return new AssertionResult(true, null, null);
    }

    public static AssertionResult fail(String failedExpression, String errorMessage) {
        return new AssertionResult(false, failedExpression, errorMessage);
    }
}
