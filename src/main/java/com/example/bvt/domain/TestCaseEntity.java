package com.example.bvt.domain;

import com.example.bvt.domain.enums.CaseStatus;
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
@Table(name = "t_test_case")
public class TestCaseEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "case_code", nullable = false, length = 64)
    private String caseCode;

    @Column(name = "case_name", nullable = false, length = 128)
    private String caseName;

    @Column(name = "engine_type", nullable = false, length = 16)
    private String engineType = "QLEXPRESS";

    @Column(name = "case_schema_version", nullable = false)
    private int caseSchemaVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_format", nullable = false, length = 8)
    private ConfigFormat configFormat;

    @Column(name = "config_content", nullable = false)
    private String configContent;

    @Column(name = "normalized_definition", nullable = false, columnDefinition = "jsonb")
    private String normalizedDefinition;

    @Column(name = "tags", columnDefinition = "jsonb")
    private String tags;

    @Column(name = "version_no", nullable = false)
    private int versionNo = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private CaseStatus status = CaseStatus.ACTIVE;

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public String getCaseCode() {
        return caseCode;
    }

    public void setCaseCode(String caseCode) {
        this.caseCode = caseCode;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public int getCaseSchemaVersion() {
        return caseSchemaVersion;
    }

    public void setCaseSchemaVersion(int caseSchemaVersion) {
        this.caseSchemaVersion = caseSchemaVersion;
    }

    public ConfigFormat getConfigFormat() {
        return configFormat;
    }

    public void setConfigFormat(ConfigFormat configFormat) {
        this.configFormat = configFormat;
    }

    public String getConfigContent() {
        return configContent;
    }

    public void setConfigContent(String configContent) {
        this.configContent = configContent;
    }

    public String getNormalizedDefinition() {
        return normalizedDefinition;
    }

    public void setNormalizedDefinition(String normalizedDefinition) {
        this.normalizedDefinition = normalizedDefinition;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }
}
