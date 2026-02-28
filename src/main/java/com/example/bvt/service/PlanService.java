package com.example.bvt.service;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.TestPlanEntity;
import com.example.bvt.domain.enums.PlanStatus;
import com.example.bvt.dto.PlanCreateRequest;
import com.example.bvt.dto.PlanEnableRequest;
import com.example.bvt.dto.PlanResponse;
import com.example.bvt.repository.TestPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanService {

    private final TestPlanRepository planRepository;
    private final ProjectService projectService;
    private final SuiteService suiteService;
    private final EnvironmentService environmentService;

    public PlanService(TestPlanRepository planRepository,
                       ProjectService projectService,
                       SuiteService suiteService,
                       EnvironmentService environmentService) {
        this.planRepository = planRepository;
        this.projectService = projectService;
        this.suiteService = suiteService;
        this.environmentService = environmentService;
    }

    @Transactional
    public PlanResponse create(PlanCreateRequest request) {
        var project = projectService.getEntity(request.projectId());
        var suite = suiteService.getEntity(request.suiteId());
        var env = environmentService.getEntity(request.environmentId());
        if (!suite.getProject().getId().equals(project.getId())) {
            throw new BusinessException("Suite not in project");
        }
        if (!env.getProject().getId().equals(project.getId())) {
            throw new BusinessException("Environment not in project");
        }
        TestPlanEntity plan = new TestPlanEntity();
        plan.setProject(project);
        plan.setSuite(suite);
        plan.setEnvironment(env);
        plan.setPlanName(request.planName());
        plan.setCronExpr(request.cronExpr());
        plan.setStatus(PlanStatus.ENABLED);
        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public PlanResponse enable(Long id, PlanEnableRequest request) {
        TestPlanEntity plan = getEntity(id);
        plan.setStatus(Boolean.TRUE.equals(request.enabled()) ? PlanStatus.ENABLED : PlanStatus.DISABLED);
        return toResponse(planRepository.save(plan));
    }

    public List<PlanResponse> list() {
        return planRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TestPlanEntity getEntity(Long id) {
        return planRepository.findById(id).orElseThrow(() -> new BusinessException("Plan not found: " + id));
    }

    private PlanResponse toResponse(TestPlanEntity plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getProject().getId(),
                plan.getPlanName(),
                plan.getSuite().getId(),
                plan.getEnvironment().getId(),
                plan.getCronExpr(),
                plan.getStatus(),
                plan.getLastRunAt()
        );
    }
}
