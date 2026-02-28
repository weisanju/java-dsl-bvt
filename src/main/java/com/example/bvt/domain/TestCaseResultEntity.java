package com.example.bvt.domain;

import com.example.bvt.domain.enums.ResultStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_test_case_result")
public class TestCaseResultEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private TestRunEntity run;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TestCaseEntity testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suite_case_id")
    private TestSuiteCaseEntity suiteCase;

    @Column(name = "step_no")
    private Integer stepNo;

    @Column(name = "step_name", length = 128)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ResultStatus status;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "assertion_failed", nullable = false)
    private int assertionFailed;

    @Column(name = "failed_assert_expr")
    private String failedAssertExpr;

    @Column(name = "request_snapshot", columnDefinition = "jsonb")
    private String requestSnapshot;

    @Column(name = "response_snapshot", columnDefinition = "jsonb")
    private String responseSnapshot;

    @Column(name = "extracted_vars", columnDefinition = "jsonb")
    private String extractedVars;

    @Column(name = "error_message")
    private String errorMessage;

    public TestRunEntity getRun() {
        return run;
    }

    public void setRun(TestRunEntity run) {
        this.run = run;
    }

    public TestCaseEntity getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCaseEntity testCase) {
        this.testCase = testCase;
    }

    public TestSuiteCaseEntity getSuiteCase() {
        return suiteCase;
    }

    public void setSuiteCase(TestSuiteCaseEntity suiteCase) {
        this.suiteCase = suiteCase;
    }

    public Integer getStepNo() {
        return stepNo;
    }

    public void setStepNo(Integer stepNo) {
        this.stepNo = stepNo;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public int getAssertionFailed() {
        return assertionFailed;
    }

    public void setAssertionFailed(int assertionFailed) {
        this.assertionFailed = assertionFailed;
    }

    public String getFailedAssertExpr() {
        return failedAssertExpr;
    }

    public void setFailedAssertExpr(String failedAssertExpr) {
        this.failedAssertExpr = failedAssertExpr;
    }

    public String getRequestSnapshot() {
        return requestSnapshot;
    }

    public void setRequestSnapshot(String requestSnapshot) {
        this.requestSnapshot = requestSnapshot;
    }

    public String getResponseSnapshot() {
        return responseSnapshot;
    }

    public void setResponseSnapshot(String responseSnapshot) {
        this.responseSnapshot = responseSnapshot;
    }

    public String getExtractedVars() {
        return extractedVars;
    }

    public void setExtractedVars(String extractedVars) {
        this.extractedVars = extractedVars;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
