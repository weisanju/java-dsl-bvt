package com.example.bvt.engine;

import com.example.bvt.config.ExecutionProperties;
import com.example.bvt.domain.ProjectEntity;
import com.example.bvt.domain.TestCaseEntity;
import com.example.bvt.domain.TestEnvironmentEntity;
import com.example.bvt.domain.TestRunEntity;
import com.example.bvt.domain.TestSuiteCaseEntity;
import com.example.bvt.domain.TestSuiteEntity;
import com.example.bvt.domain.enums.TriggerType;
import com.example.bvt.engine.model.AssertionResult;
import com.example.bvt.engine.model.CaseConfig;
import com.example.bvt.engine.model.HttpResponseData;
import com.example.bvt.engine.model.RenderedRequest;
import com.example.bvt.engine.model.RequestConfig;
import com.example.bvt.engine.model.StepConfig;
import com.example.bvt.repository.EnvVariableRepository;
import com.example.bvt.repository.TestCaseResultRepository;
import com.example.bvt.repository.TestRunRepository;
import com.example.bvt.repository.TestSuiteCaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrchestratorEngineTest {

    @Test
    void shouldFailFastPerCaseAndContinueSuite() {
        TestRunRepository runRepository = Mockito.mock(TestRunRepository.class);
        TestSuiteCaseRepository suiteCaseRepository = Mockito.mock(TestSuiteCaseRepository.class);
        TestCaseResultRepository resultRepository = Mockito.mock(TestCaseResultRepository.class);
        EnvVariableRepository variableRepository = Mockito.mock(EnvVariableRepository.class);
        ConfigParserEngine configParserEngine = Mockito.mock(ConfigParserEngine.class);
        ContextEngine contextEngine = Mockito.mock(ContextEngine.class);
        RequestRenderEngine requestRenderEngine = Mockito.mock(RequestRenderEngine.class);
        HttpExecuteEngine httpExecuteEngine = Mockito.mock(HttpExecuteEngine.class);
        AssertEngine assertEngine = Mockito.mock(AssertEngine.class);
        ExtractEngine extractEngine = Mockito.mock(ExtractEngine.class);
        QlExpressEngine qlExpressEngine = Mockito.mock(QlExpressEngine.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ExecutionProperties props = new ExecutionProperties();

        OrchestratorEngine orchestrator = new OrchestratorEngine(
                runRepository,
                suiteCaseRepository,
                resultRepository,
                variableRepository,
                configParserEngine,
                contextEngine,
                requestRenderEngine,
                httpExecuteEngine,
                assertEngine,
                extractEngine,
                qlExpressEngine,
                objectMapper,
                props
        );

        TestRunEntity run = buildRunEntity();
        TestSuiteCaseEntity suiteCase1 = buildSuiteCase(run.getSuite(), "CASE_1", "{\"meta\":{\"caseCode\":\"CASE_1\",\"caseName\":\"c1\"},\"steps\":[]}");
        TestSuiteCaseEntity suiteCase2 = buildSuiteCase(run.getSuite(), "CASE_2", "{\"meta\":{\"caseCode\":\"CASE_2\",\"caseName\":\"c2\"},\"steps\":[]}");

        CaseConfig caseConfig1 = new CaseConfig(
                new CaseConfig.Meta("CASE_1", "case1", 1, List.of()),
                List.of(
                        new StepConfig("s1", new RequestConfig("GET", "'http://localhost'", Map.of(), null, 1000),
                                List.of("status == 200"), Map.of(), null),
                        new StepConfig("s2", new RequestConfig("GET", "'http://localhost/next'", Map.of(), null, 1000),
                                List.of("status == 200"), Map.of(), null)
                )
        );
        CaseConfig caseConfig2 = new CaseConfig(
                new CaseConfig.Meta("CASE_2", "case2", 1, List.of()),
                List.of(new StepConfig("s1", new RequestConfig("GET", "'http://localhost'", Map.of(), null, 1000),
                        List.of("status == 200"), Map.of(), null))
        );

        when(runRepository.findById(anyLong())).thenReturn(Optional.of(run));
        when(runRepository.save(any(TestRunEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(suiteCaseRepository.findBySuiteIdAndEnabledTrueOrderByExecOrderAscIdAsc(any())).thenReturn(List.of(suiteCase1, suiteCase2));
        when(variableRepository.findByProjectIdAndEnvironmentIsNullOrderByIdAsc(any())).thenReturn(List.of());
        when(variableRepository.findByProjectIdAndEnvironmentIdOrderByIdAsc(any(), any())).thenReturn(List.of());
        when(configParserEngine.parseNormalizedCaseConfig(suiteCase1.getTestCase().getNormalizedDefinition())).thenReturn(caseConfig1);
        when(configParserEngine.parseNormalizedCaseConfig(suiteCase2.getTestCase().getNormalizedDefinition())).thenReturn(caseConfig2);
        when(contextEngine.buildContext(any(), anyList(), anyList(), any())).thenReturn(new HashMap<>());
        when(requestRenderEngine.render(any(), anyMap())).thenReturn(
                new RenderedRequest("GET", "http://localhost", Map.of(), null, Duration.ofMillis(1000)));
        when(httpExecuteEngine.execute(any())).thenReturn(new HttpResponseData(200, Map.of(), "{\"code\":0}", 10));
        when(assertEngine.evaluate(any(), anyMap(), any())).thenReturn(
                AssertionResult.fail("status == 200", "false"),
                AssertionResult.pass()
        );
        when(extractEngine.extract(any(), anyMap(), any())).thenReturn(Map.of());
        when(resultRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TestRunEntity finished = orchestrator.executeRun(1L);

        Assertions.assertEquals(2, finished.getTotalCases());
        Assertions.assertEquals(1, finished.getFailedCases());
        Assertions.assertEquals(1, finished.getPassedCases());
        verify(requestRenderEngine, times(2)).render(any(), anyMap());
        verify(assertEngine, times(2)).evaluate(any(), anyMap(), any());
        verify(resultRepository, times(2)).save(any());
    }

    private TestRunEntity buildRunEntity() {
        ProjectEntity project = new ProjectEntity();
        TestEnvironmentEntity environment = new TestEnvironmentEntity();
        environment.setProject(project);
        environment.setEnvCode("test");
        environment.setBaseUrl("http://localhost");
        TestSuiteEntity suite = new TestSuiteEntity();
        suite.setProject(project);
        suite.setSuiteCode("S1");
        suite.setSuiteName("suite");
        TestRunEntity run = new TestRunEntity();
        run.setProject(project);
        run.setEnvironment(environment);
        run.setSuite(suite);
        run.setTriggerType(TriggerType.MANUAL);
        return run;
    }

    private TestSuiteCaseEntity buildSuiteCase(TestSuiteEntity suite, String caseCode, String normalizedDefinition) {
        TestCaseEntity testCase = new TestCaseEntity();
        testCase.setCaseCode(caseCode);
        testCase.setCaseName(caseCode);
        testCase.setNormalizedDefinition(normalizedDefinition);
        TestSuiteCaseEntity suiteCase = new TestSuiteCaseEntity();
        suiteCase.setSuite(suite);
        suiteCase.setTestCase(testCase);
        suiteCase.setEnabled(true);
        return suiteCase;
    }
}
