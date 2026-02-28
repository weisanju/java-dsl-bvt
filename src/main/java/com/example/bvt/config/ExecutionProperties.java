package com.example.bvt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.execution")
public class ExecutionProperties {

    private int stepTimeoutMs = 5000;
    private int caseTimeoutMs = 60000;
    private int snapshotMaxChars = 16384;

    public int getStepTimeoutMs() {
        return stepTimeoutMs;
    }

    public void setStepTimeoutMs(int stepTimeoutMs) {
        this.stepTimeoutMs = stepTimeoutMs;
    }

    public int getCaseTimeoutMs() {
        return caseTimeoutMs;
    }

    public void setCaseTimeoutMs(int caseTimeoutMs) {
        this.caseTimeoutMs = caseTimeoutMs;
    }

    public int getSnapshotMaxChars() {
        return snapshotMaxChars;
    }

    public void setSnapshotMaxChars(int snapshotMaxChars) {
        this.snapshotMaxChars = snapshotMaxChars;
    }
}
