package com.example.bvt.service;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.ProjectEntity;
import com.example.bvt.domain.TestEnvironmentEntity;
import com.example.bvt.dto.EnvironmentCreateRequest;
import com.example.bvt.dto.EnvironmentResponse;
import com.example.bvt.repository.TestEnvironmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnvironmentService {

    private final TestEnvironmentRepository environmentRepository;
    private final ProjectService projectService;

    public EnvironmentService(TestEnvironmentRepository environmentRepository, ProjectService projectService) {
        this.environmentRepository = environmentRepository;
        this.projectService = projectService;
    }

    @Transactional
    public EnvironmentResponse create(EnvironmentCreateRequest request) {
        ProjectEntity project = projectService.getEntity(request.projectId());
        environmentRepository.findByProjectIdAndEnvCode(project.getId(), request.envCode()).ifPresent(existing -> {
            throw new BusinessException("Environment code already exists");
        });
        if (request.isDefault()) {
            clearDefault(project.getId());
        }
        TestEnvironmentEntity entity = new TestEnvironmentEntity();
        entity.setProject(project);
        entity.setEnvCode(request.envCode());
        entity.setEnvName(request.envName());
        entity.setBaseUrl(request.baseUrl());
        entity.setDefault(request.isDefault());
        return toResponse(environmentRepository.save(entity));
    }

    public List<EnvironmentResponse> list(Long projectId) {
        return environmentRepository.findByProjectIdOrderByIdAsc(projectId).stream().map(this::toResponse).toList();
    }

    public TestEnvironmentEntity getEntity(Long environmentId) {
        return environmentRepository.findById(environmentId)
                .orElseThrow(() -> new BusinessException("Environment not found: " + environmentId));
    }

    private void clearDefault(Long projectId) {
        List<TestEnvironmentEntity> list = environmentRepository.findByProjectIdOrderByIdAsc(projectId);
        for (TestEnvironmentEntity env : list) {
            if (env.isDefault()) {
                env.setDefault(false);
                environmentRepository.save(env);
            }
        }
    }

    private EnvironmentResponse toResponse(TestEnvironmentEntity entity) {
        return new EnvironmentResponse(
                entity.getId(),
                entity.getProject().getId(),
                entity.getEnvCode(),
                entity.getEnvName(),
                entity.getBaseUrl(),
                entity.isDefault()
        );
    }
}
