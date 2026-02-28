package com.example.bvt.engine;

import com.example.bvt.domain.EnvVariableEntity;
import com.example.bvt.domain.TestCaseDataSetEntity;
import com.example.bvt.domain.TestEnvironmentEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContextEngine {

    private final ConfigParserEngine configParserEngine;

    public ContextEngine(ConfigParserEngine configParserEngine) {
        this.configParserEngine = configParserEngine;
    }

    public Map<String, Object> buildContext(
            TestEnvironmentEntity environment,
            List<EnvVariableEntity> projectVariables,
            List<EnvVariableEntity> environmentVariables,
            TestCaseDataSetEntity dataSet) {
        Map<String, Object> context = new HashMap<>();
        context.put("env", environment.getEnvCode());
        context.put("baseUrl", environment.getBaseUrl());

        for (EnvVariableEntity variable : projectVariables) {
            context.put(variable.getVarKey(), variable.getVarValue());
        }
        for (EnvVariableEntity variable : environmentVariables) {
            context.put(variable.getVarKey(), variable.getVarValue());
        }
        if (dataSet != null) {
            Map<String, Object> dataMap = configParserEngine.parseDataContent(dataSet.getDataFormat(), dataSet.getDataContent());
            context.putAll(dataMap);
        }
        return context;
    }
}
