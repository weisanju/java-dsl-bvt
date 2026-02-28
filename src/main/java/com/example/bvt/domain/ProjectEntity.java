package com.example.bvt.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_project")
public class ProjectEntity extends BaseEntity {

    @Column(name = "project_key", nullable = false, unique = true, length = 64)
    private String projectKey;

    @Column(name = "project_name", nullable = false, length = 128)
    private String projectName;

    @Column(name = "description")
    private String description;

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
