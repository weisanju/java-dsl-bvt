package com.example.bvt.service;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.ProjectEntity;
import com.example.bvt.domain.TestCaseEntity;
import com.example.bvt.dto.CaseResponse;
import com.example.bvt.dto.CaseUpsertRequest;
import com.example.bvt.engine.ConfigParserEngine;
import com.example.bvt.engine.model.ParsedCaseConfig;
import com.example.bvt.repository.TestCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TestCaseService {

    private final TestCaseRepository caseRepository;
    private final ProjectService projectService;
    private final ConfigParserEngine configParserEngine;

    public TestCaseService(TestCaseRepository caseRepository,
                           ProjectService projectService,
                           ConfigParserEngine configParserEngine) {
        this.caseRepository = caseRepository;
        this.projectService = projectService;
        this.configParserEngine = configParserEngine;
    }

    @Transactional
    public CaseResponse create(CaseUpsertRequest request) {
        ProjectEntity project = projectService.getEntity(request.projectId());
        caseRepository.findByProjectIdAndCaseCode(project.getId(), request.caseCode()).ifPresent(it -> {
            throw new BusinessException("Case code already exists");
        });
        ParsedCaseConfig parsed = configParserEngine.parseCaseConfig(request.configFormat(), request.configContent());
        TestCaseEntity entity = new TestCaseEntity();
        entity.setProject(project);
        entity.setCaseCode(request.caseCode());
        entity.setCaseName(request.caseName());
        entity.setConfigFormat(request.configFormat());
        entity.setConfigContent(request.configContent());
        entity.setNormalizedDefinition(parsed.normalizedDefinition());
        entity.setTags(request.tags());
        return toResponse(caseRepository.save(entity));
    }

    @Transactional
    public CaseResponse update(Long id, CaseUpsertRequest request) {
        TestCaseEntity entity = getEntity(id);
        if (!entity.getProject().getId().equals(request.projectId())) {
            throw new BusinessException("Case project mismatch");
        }
        ParsedCaseConfig parsed = configParserEngine.parseCaseConfig(request.configFormat(), request.configContent());
        entity.setCaseName(request.caseName());
        entity.setConfigFormat(request.configFormat());
        entity.setConfigContent(request.configContent());
        entity.setNormalizedDefinition(parsed.normalizedDefinition());
        entity.setTags(request.tags());
        entity.setVersionNo(entity.getVersionNo() + 1);
        return toResponse(caseRepository.save(entity));
    }

    public List<CaseResponse> list(Long projectId) {
        return caseRepository.findByProjectIdOrderByIdAsc(projectId).stream().map(this::toResponse).toList();
    }

    public TestCaseEntity getEntity(Long id) {
        return caseRepository.findById(id).orElseThrow(() -> new BusinessException("Case not found: " + id));
    }

    private CaseResponse toResponse(TestCaseEntity entity) {
        return new CaseResponse(
                entity.getId(),
                entity.getProject().getId(),
                entity.getCaseCode(),
                entity.getCaseName(),
                entity.getConfigFormat(),
                entity.getConfigContent(),
                entity.getNormalizedDefinition(),
                entity.getVersionNo(),
                entity.getStatus()
        );
    }
}
