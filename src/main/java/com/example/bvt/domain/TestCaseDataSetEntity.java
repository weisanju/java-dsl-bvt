package com.example.bvt.domain;

import com.example.bvt.domain.enums.ConfigFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_test_case_data_set")
public class TestCaseDataSetEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TestCaseEntity testCase;

    @Column(name = "data_set_name", nullable = false, length = 128)
    private String dataSetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_format", nullable = false, length = 8)
    private ConfigFormat dataFormat;

    @Column(name = "data_content", nullable = false)
    private String dataContent;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public TestCaseEntity getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCaseEntity testCase) {
        this.testCase = testCase;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public ConfigFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(ConfigFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getDataContent() {
        return dataContent;
    }

    public void setDataContent(String dataContent) {
        this.dataContent = dataContent;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
