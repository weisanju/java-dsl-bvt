package com.example.bvt.engine;

import com.example.bvt.common.BusinessException;
import com.example.bvt.config.ExecutionProperties;
import com.example.bvt.domain.EnvVariableEntity;
import com.example.bvt.domain.TestCaseEntity;
import com.example.bvt.domain.TestCaseResultEntity;
import com.example.bvt.domain.TestRunEntity;
import com.example.bvt.domain.TestSuiteCaseEntity;
import com.example.bvt.domain.enums.ResultStatus;
import com.example.bvt.domain.enums.RunStatus;
import com.example.bvt.engine.model.AssertionResult;
import com.example.bvt.engine.model.CaseConfig;
import com.example.bvt.engine.model.HttpResponseData;
import com.example.bvt.engine.model.RenderedRequest;
import com.example.bvt.engine.model.StepConfig;
import com.example.bvt.repository.EnvVariableRepository;
import com.example.bvt.repository.TestCaseResultRepository;
import com.example.bvt.repository.TestRunRepository;
import com.example.bvt.repository.TestSuiteCaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrchestratorEngine {

    private final TestRunRepository runRepository;
    private final TestSuiteCaseRepository suiteCaseRepository;
    private final TestCaseResultRepository resultRepository;
    private final EnvVariableRepository variableRepository;
    private final ConfigParserEngine configParserEngine;
    private final ContextEngine contextEngine;
    private final RequestRenderEngine requestRenderEngine;
    private final HttpExecuteEngine httpExecuteEngine;
    private final AssertEngine assertEngine;
    private final ExtractEngine extractEngine;
    private final QlExpressEngine qlExpressEngine;
    private final ObjectMapper objectMapper;
    private final ExecutionProperties executionProperties;

    public OrchestratorEngine(TestRunRepository runRepository,
                              TestSuiteCaseRepository suiteCaseRepository,
                              TestCaseResultRepository resultRepository,
                              EnvVariableRepository variableRepository,
                              ConfigParserEngine configParserEngine,
                              ContextEngine contextEngine,
                              RequestRenderEngine requestRenderEngine,
                              HttpExecuteEngine httpExecuteEngine,
                              AssertEngine assertEngine,
                              ExtractEngine extractEngine,
                              QlExpressEngine qlExpressEngine,
                              ObjectMapper objectMapper,
                              ExecutionProperties executionProperties) {
        this.runRepository = runRepository;
        this.suiteCaseRepository = suiteCaseRepository;
        this.resultRepository = resultRepository;
        this.variableRepository = variableRepository;
        this.configParserEngine = configParserEngine;
        this.contextEngine = contextEngine;
        this.requestRenderEngine = requestRenderEngine;
        this.httpExecuteEngine = httpExecuteEngine;
        this.assertEngine = assertEngine;
        this.extractEngine = extractEngine;
        this.qlExpressEngine = qlExpressEngine;
        this.objectMapper = objectMapper;
        this.executionProperties = executionProperties;
    }

    public TestRunEntity executeRun(Long runId) {
        TestRunEntity run = runRepository.findById(runId)
                .orElseThrow(() -> new BusinessException("Run not found: " + runId));
        if (run.getStatus() != RunStatus.QUEUED && run.getStatus() != RunStatus.RUNNING) {
            return run;
        }

        run.setStatus(RunStatus.RUNNING);
        run.setStartedAt(Instant.now());
        runRepository.save(run);

        List<TestSuiteCaseEntity> suiteCases = suiteCaseRepository
                .findBySuiteIdAndEnabledTrueOrderByExecOrderAscIdAsc(run.getSuite().getId());
        List<EnvVariableEntity> projectVariables = variableRepository
                .findByProjectIdAndEnvironmentIsNullOrderByIdAsc(run.getProject().getId());
        List<EnvVariableEntity> envVariables = variableRepository
                .findByProjectIdAndEnvironmentIdOrderByIdAsc(run.getProject().getId(), run.getEnvironment().getId());

        int passedCases = 0;
        int failedCases = 0;
        int skippedCases = 0;
        String firstError = null;

        for (TestSuiteCaseEntity suiteCase : suiteCases) {
            CaseRunStatus status = executeCase(run, suiteCase, projectVariables, envVariables);
            switch (status) {
                case PASSED -> passedCases++;
                case FAILED -> {
                    failedCases++;
                    if (firstError == null) {
                        firstError = "Case failed: " + suiteCase.getTestCase().getCaseCode();
                    }
                }
                case SKIPPED -> skippedCases++;
            }
        }

        run.setTotalCases(suiteCases.size());
        run.setPassedCases(passedCases);
        run.setFailedCases(failedCases);
        run.setSkippedCases(skippedCases);
        run.setStatus(failedCases > 0 ? RunStatus.FAILED : RunStatus.SUCCESS);
        run.setErrorSummary(firstError);
        run.setFinishedAt(Instant.now());
        run.setDurationMs(Duration.between(run.getStartedAt(), run.getFinishedAt()).toMillis());
        return runRepository.save(run);
    }

    private CaseRunStatus executeCase(TestRunEntity run,
                                      TestSuiteCaseEntity suiteCase,
                                      List<EnvVariableEntity> projectVariables,
                                      List<EnvVariableEntity> envVariables) {
        TestCaseEntity testCase = suiteCase.getTestCase();
        CaseConfig caseConfig = configParserEngine.parseNormalizedCaseConfig(testCase.getNormalizedDefinition());
        Map<String, Object> context = contextEngine.buildContext(
                run.getEnvironment(),
                projectVariables,
                envVariables,
                suiteCase.getDataSet()
        );

        boolean caseFailed = false;
        boolean allStepsSkipped = true;
        Instant caseStart = Instant.now();
        int stepNo = 0;
        for (StepConfig step : caseConfig.steps()) {
            stepNo++;
            if (Duration.between(caseStart, Instant.now()).toMillis() > executionProperties.getCaseTimeoutMs()) {
                saveErrorResult(run, suiteCase, stepNo, step.name(), "Case timeout");
                caseFailed = true;
                break;
            }
            if (step.skipWhen() != null && !step.skipWhen().isBlank()) {
                boolean skip = qlExpressEngine.evalBoolean(step.skipWhen(), context);
                if (skip) {
                    saveSkippedResult(run, suiteCase, stepNo, step.name());
                    continue;
                }
            }
            allStepsSkipped = false;
            try {
                RenderedRequest renderedRequest = requestRenderEngine.render(step, context);
                HttpResponseData responseData = httpExecuteEngine.execute(renderedRequest);
                AssertionResult assertionResult = assertEngine.evaluate(step.assertions(), context, responseData);
                Map<String, Object> extractedVars = extractEngine.extract(step.extracts(), context, responseData);
                if (!assertionResult.passed()) {
                    saveFailedResult(run, suiteCase, stepNo, step.name(), renderedRequest, responseData, extractedVars,
                            assertionResult.failedExpression(), assertionResult.errorMessage());
                    caseFailed = true; // step 级 fail-fast
                    break;
                }
                savePassedResult(run, suiteCase, stepNo, step.name(), renderedRequest, responseData, extractedVars);
            } catch (Exception ex) {
                saveErrorResult(run, suiteCase, stepNo, step.name(), ex.getMessage());
                caseFailed = true;
                break;
            }
        }

        if (caseFailed) {
            return CaseRunStatus.FAILED;
        }
        if (allStepsSkipped) {
            return CaseRunStatus.SKIPPED;
        }
        return CaseRunStatus.PASSED;
    }

    private void savePassedResult(TestRunEntity run,
                                  TestSuiteCaseEntity suiteCase,
                                  int stepNo,
                                  String stepName,
                                  RenderedRequest request,
                                  HttpResponseData response,
                                  Map<String, Object> extractedVars) {
        TestCaseResultEntity entity = baseResult(run, suiteCase, stepNo, stepName);
        entity.setStatus(ResultStatus.PASSED);
        entity.setResponseTimeMs((int) response.elapsedMs());
        entity.setAssertionFailed(0);
        entity.setRequestSnapshot(toJsonSnapshot(requestSnapshotMap(request)));
        entity.setResponseSnapshot(toJsonSnapshot(responseSnapshotMap(response)));
        entity.setExtractedVars(toJsonSnapshot(extractedVars));
        resultRepository.save(entity);
    }

    private void saveFailedResult(TestRunEntity run,
                                  TestSuiteCaseEntity suiteCase,
                                  int stepNo,
                                  String stepName,
                                  RenderedRequest request,
                                  HttpResponseData response,
                                  Map<String, Object> extractedVars,
                                  String failedExpression,
                                  String errorMessage) {
        TestCaseResultEntity entity = baseResult(run, suiteCase, stepNo, stepName);
        entity.setStatus(ResultStatus.FAILED);
        entity.setResponseTimeMs((int) response.elapsedMs());
        entity.setAssertionFailed(1);
        entity.setFailedAssertExpr(failedExpression);
        entity.setErrorMessage(errorMessage);
        entity.setRequestSnapshot(toJsonSnapshot(requestSnapshotMap(request)));
        entity.setResponseSnapshot(toJsonSnapshot(responseSnapshotMap(response)));
        entity.setExtractedVars(toJsonSnapshot(extractedVars));
        resultRepository.save(entity);
    }

    private void saveSkippedResult(TestRunEntity run, TestSuiteCaseEntity suiteCase, int stepNo, String stepName) {
        TestCaseResultEntity entity = baseResult(run, suiteCase, stepNo, stepName);
        entity.setStatus(ResultStatus.SKIPPED);
        entity.setAssertionFailed(0);
        resultRepository.save(entity);
    }

    private void saveErrorResult(TestRunEntity run, TestSuiteCaseEntity suiteCase, int stepNo, String stepName, String message) {
        TestCaseResultEntity entity = baseResult(run, suiteCase, stepNo, stepName);
        entity.setStatus(ResultStatus.ERROR);
        entity.setAssertionFailed(1);
        entity.setErrorMessage(message);
        resultRepository.save(entity);
    }

    private TestCaseResultEntity baseResult(TestRunEntity run, TestSuiteCaseEntity suiteCase, int stepNo, String stepName) {
        TestCaseResultEntity entity = new TestCaseResultEntity();
        entity.setRun(run);
        entity.setTestCase(suiteCase.getTestCase());
        entity.setSuiteCase(suiteCase);
        entity.setStepNo(stepNo);
        entity.setStepName(stepName);
        return entity;
    }

    private String toJsonSnapshot(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{\"error\":\"snapshot serialize failed\"}";
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        int max = executionProperties.getSnapshotMaxChars();
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }

    private Map<String, Object> requestSnapshotMap(RenderedRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("method", request.method());
        map.put("url", request.url());
        map.put("headers", request.headers());
        map.put("body", trim(request.body()));
        return map;
    }

    private Map<String, Object> responseSnapshotMap(HttpResponseData response) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", response.status());
        map.put("headers", response.headers());
        map.put("body", trim(response.body()));
        return map;
    }

    private enum CaseRunStatus {
        PASSED, FAILED, SKIPPED
    }
}
