package com.example.bvt.service;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.ProjectEntity;
import com.example.bvt.domain.TestCaseDataSetEntity;
import com.example.bvt.domain.TestCaseEntity;
import com.example.bvt.domain.TestSuiteCaseEntity;
import com.example.bvt.domain.TestSuiteEntity;
import com.example.bvt.dto.SuiteBindCaseRequest;
import com.example.bvt.dto.SuiteCaseResponse;
import com.example.bvt.dto.SuiteCreateRequest;
import com.example.bvt.dto.SuiteResponse;
import com.example.bvt.repository.TestCaseDataSetRepository;
import com.example.bvt.repository.TestSuiteCaseRepository;
import com.example.bvt.repository.TestSuiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SuiteService {

    private final TestSuiteRepository suiteRepository;
    private final TestSuiteCaseRepository suiteCaseRepository;
    private final TestCaseDataSetRepository dataSetRepository;
    private final ProjectService projectService;
    private final TestCaseService testCaseService;

    public SuiteService(TestSuiteRepository suiteRepository,
                        TestSuiteCaseRepository suiteCaseRepository,
                        TestCaseDataSetRepository dataSetRepository,
                        ProjectService projectService,
                        TestCaseService testCaseService) {
        this.suiteRepository = suiteRepository;
        this.suiteCaseRepository = suiteCaseRepository;
        this.dataSetRepository = dataSetRepository;
        this.projectService = projectService;
        this.testCaseService = testCaseService;
    }

    public SuiteResponse create(SuiteCreateRequest request) {
        ProjectEntity project = projectService.getEntity(request.projectId());
        suiteRepository.findByProjectIdAndSuiteCode(project.getId(), request.suiteCode()).ifPresent(existing -> {
            throw new BusinessException("Suite code already exists");
        });
        TestSuiteEntity entity = new TestSuiteEntity();
        entity.setProject(project);
        entity.setSuiteCode(request.suiteCode());
        entity.setSuiteName(request.suiteName());
        entity.setDescription(request.description());
        return toResponse(suiteRepository.save(entity));
    }

    public List<SuiteResponse> list(Long projectId) {
        return suiteRepository.findByProjectIdOrderByIdAsc(projectId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public SuiteCaseResponse bindCase(Long suiteId, SuiteBindCaseRequest request) {
        TestSuiteEntity suite = getEntity(suiteId);
        TestCaseEntity testCase = testCaseService.getEntity(request.caseId());
        if (!suite.getProject().getId().equals(testCase.getProject().getId())) {
            throw new BusinessException("Case not in suite project");
        }
        TestCaseDataSetEntity dataSet = null;
        if (request.dataSetId() != null) {
            dataSet = dataSetRepository.findById(request.dataSetId())
                    .orElseThrow(() -> new BusinessException("Data set not found: " + request.dataSetId()));
            if (!dataSet.getTestCase().getId().equals(testCase.getId())) {
                throw new BusinessException("Data set not belongs to case");
            }
        }

        TestSuiteCaseEntity entity = new TestSuiteCaseEntity();
        entity.setSuite(suite);
        entity.setTestCase(testCase);
        entity.setDataSet(dataSet);
        entity.setExecOrder(request.execOrder());
        entity.setEnabled(request.enabled());
        return toResponse(suiteCaseRepository.save(entity));
    }

    public List<SuiteCaseResponse> listSuiteCases(Long suiteId) {
        return suiteCaseRepository.findBySuiteIdAndEnabledTrueOrderByExecOrderAscIdAsc(suiteId)
                .stream().map(this::toResponse).toList();
    }

    public TestSuiteEntity getEntity(Long id) {
        return suiteRepository.findById(id).orElseThrow(() -> new BusinessException("Suite not found: " + id));
    }

    private SuiteResponse toResponse(TestSuiteEntity entity) {
        return new SuiteResponse(
                entity.getId(),
                entity.getProject().getId(),
                entity.getSuiteCode(),
                entity.getSuiteName(),
                entity.getDescription(),
                entity.getStatus()
        );
    }

    private SuiteCaseResponse toResponse(TestSuiteCaseEntity entity) {
        return new SuiteCaseResponse(
                entity.getId(),
                entity.getSuite().getId(),
                entity.getTestCase().getId(),
                entity.getDataSet() == null ? null : entity.getDataSet().getId(),
                entity.getExecOrder(),
                entity.isEnabled()
        );
    }
}
