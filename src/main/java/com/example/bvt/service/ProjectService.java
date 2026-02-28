package com.example.bvt.service;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.ProjectEntity;
import com.example.bvt.dto.ProjectCreateRequest;
import com.example.bvt.dto.ProjectResponse;
import com.example.bvt.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectResponse create(ProjectCreateRequest request) {
        projectRepository.findByProjectKey(request.projectKey()).ifPresent(project -> {
            throw new BusinessException("Project key already exists");
        });
        ProjectEntity entity = new ProjectEntity();
        entity.setProjectKey(request.projectKey());
        entity.setProjectName(request.projectName());
        entity.setDescription(request.description());
        return toResponse(projectRepository.save(entity));
    }

    public List<ProjectResponse> list() {
        return projectRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProjectEntity getEntity(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new BusinessException("Project not found: " + id));
    }

    private ProjectResponse toResponse(ProjectEntity entity) {
        return new ProjectResponse(entity.getId(), entity.getProjectKey(), entity.getProjectName(), entity.getDescription());
    }
}
