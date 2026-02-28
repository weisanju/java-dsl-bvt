package com.example.bvt.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_test_suite_case")
public class TestSuiteCaseEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suite_id", nullable = false)
    private TestSuiteEntity suite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TestCaseEntity testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_set_id")
    private TestCaseDataSetEntity dataSet;

    @Column(name = "exec_order", nullable = false)
    private int execOrder;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    public TestSuiteEntity getSuite() {
        return suite;
    }

    public void setSuite(TestSuiteEntity suite) {
        this.suite = suite;
    }

    public TestCaseEntity getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCaseEntity testCase) {
        this.testCase = testCase;
    }

    public TestCaseDataSetEntity getDataSet() {
        return dataSet;
    }

    public void setDataSet(TestCaseDataSetEntity dataSet) {
        this.dataSet = dataSet;
    }

    public int getExecOrder() {
        return execOrder;
    }

    public void setExecOrder(int execOrder) {
        this.execOrder = execOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
