package com.example.bvt.service;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.EnvVariableEntity;
import com.example.bvt.domain.ProjectEntity;
import com.example.bvt.domain.TestEnvironmentEntity;
import com.example.bvt.dto.VariableResponse;
import com.example.bvt.dto.VariableUpsertRequest;
import com.example.bvt.repository.EnvVariableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VariableService {

    private final EnvVariableRepository variableRepository;
    private final ProjectService projectService;
    private final EnvironmentService environmentService;

    public VariableService(EnvVariableRepository variableRepository,
                           ProjectService projectService,
                           EnvironmentService environmentService) {
        this.variableRepository = variableRepository;
        this.projectService = projectService;
        this.environmentService = environmentService;
    }

    @Transactional
    public VariableResponse upsert(VariableUpsertRequest request) {
        ProjectEntity project = projectService.getEntity(request.projectId());
        TestEnvironmentEntity environment = null;
        if (request.environmentId() != null) {
            environment = environmentService.getEntity(request.environmentId());
            if (!environment.getProject().getId().equals(project.getId())) {
                throw new BusinessException("Environment not in project");
            }
        }

        Optional<EnvVariableEntity> existing = environment == null
                ? variableRepository.findByProjectIdAndEnvironmentIsNullAndVarKey(project.getId(), request.varKey())
                : variableRepository.findByProjectIdAndEnvironmentIdAndVarKey(project.getId(), environment.getId(), request.varKey());

        EnvVariableEntity entity = existing.orElseGet(EnvVariableEntity::new);
        entity.setProject(project);
        entity.setEnvironment(environment);
        entity.setVarKey(request.varKey());
        entity.setVarValue(request.varValue());
        return toResponse(variableRepository.save(entity));
    }

    public List<VariableResponse> list(Long projectId, Long environmentId) {
        if (environmentId == null) {
            return variableRepository.findByProjectIdAndEnvironmentIsNullOrderByIdAsc(projectId).stream().map(this::toResponse).toList();
        }
        List<EnvVariableEntity> globals = variableRepository.findByProjectIdAndEnvironmentIsNullOrderByIdAsc(projectId);
        List<EnvVariableEntity> envVars = variableRepository.findByProjectIdAndEnvironmentIdOrderByIdAsc(projectId, environmentId);
        Map<String, EnvVariableEntity> merged = new LinkedHashMap<>();
        globals.forEach(v -> merged.put(v.getVarKey(), v));
        envVars.forEach(v -> merged.put(v.getVarKey(), v));
        List<VariableResponse> responses = new ArrayList<>();
        merged.values().forEach(value -> responses.add(toResponse(value)));
        return responses;
    }

    private VariableResponse toResponse(EnvVariableEntity entity) {
        return new VariableResponse(
                entity.getId(),
                entity.getProject().getId(),
                entity.getEnvironment() == null ? null : entity.getEnvironment().getId(),
                entity.getVarKey(),
                entity.getVarValue()
        );
    }
}
