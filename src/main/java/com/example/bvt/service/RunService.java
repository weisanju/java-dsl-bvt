package com.example.bvt.service;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.TestRunEntity;
import com.example.bvt.domain.TestSuiteEntity;
import com.example.bvt.domain.enums.RunStatus;
import com.example.bvt.domain.enums.TriggerType;
import com.example.bvt.dto.RunResponse;
import com.example.bvt.engine.OrchestratorEngine;
import com.example.bvt.repository.TestRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RunService {

    private final TestRunRepository runRepository;
    private final SuiteService suiteService;
    private final EnvironmentService environmentService;
    private final OrchestratorEngine orchestratorEngine;

    public RunService(TestRunRepository runRepository,
                      SuiteService suiteService,
                      EnvironmentService environmentService,
                      OrchestratorEngine orchestratorEngine) {
        this.runRepository = runRepository;
        this.suiteService = suiteService;
        this.environmentService = environmentService;
        this.orchestratorEngine = orchestratorEngine;
    }

    @Transactional
    public RunResponse triggerManual(Long suiteId, Long environmentId) {
        TestSuiteEntity suite = suiteService.getEntity(suiteId);
        var environment = environmentService.getEntity(environmentId);
        if (!suite.getProject().getId().equals(environment.getProject().getId())) {
            throw new BusinessException("Environment not in suite project");
        }
        TestRunEntity run = new TestRunEntity();
        run.setProject(suite.getProject());
        run.setSuite(suite);
        run.setEnvironment(environment);
        run.setTriggerType(TriggerType.MANUAL);
        run.setStatus(RunStatus.QUEUED);
        TestRunEntity saved = runRepository.save(run);
        TestRunEntity finished = orchestratorEngine.executeRun(saved.getId());
        return toResponse(finished);
    }

    public RunResponse getRun(Long runId) {
        return toResponse(getEntity(runId));
    }

    public TestRunEntity getEntity(Long runId) {
        return runRepository.findById(runId).orElseThrow(() -> new BusinessException("Run not found: " + runId));
    }

    public TestRunEntity triggerByPlan(Long suiteId, Long environmentId) {
        TestSuiteEntity suite = suiteService.getEntity(suiteId);
        var environment = environmentService.getEntity(environmentId);
        TestRunEntity run = new TestRunEntity();
        run.setProject(suite.getProject());
        run.setSuite(suite);
        run.setEnvironment(environment);
        run.setTriggerType(TriggerType.CRON);
        run.setStatus(RunStatus.QUEUED);
        run = runRepository.save(run);
        return orchestratorEngine.executeRun(run.getId());
    }

    private RunResponse toResponse(TestRunEntity entity) {
        return new RunResponse(
                entity.getId(),
                entity.getProject().getId(),
                entity.getSuite().getId(),
                entity.getEnvironment().getId(),
                entity.getTriggerType(),
                entity.getStatus(),
                entity.getTotalCases(),
                entity.getPassedCases(),
                entity.getFailedCases(),
                entity.getSkippedCases(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getDurationMs(),
                entity.getErrorSummary()
        );
    }
}
