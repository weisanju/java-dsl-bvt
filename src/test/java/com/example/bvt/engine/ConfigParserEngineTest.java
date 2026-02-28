package com.example.bvt.engine;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.enums.ConfigFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigParserEngineTest {

    private final ConfigParserEngine engine = new ConfigParserEngine(new ObjectMapper(), new ObjectMapper(new YAMLFactory()));

    @Test
    void shouldParseJsonConfigSuccessfully() {
        String json = """
                {
                  "meta": {"caseCode":"C1","caseName":"case1"},
                  "steps":[
                    {"name":"s1","request":{"method":"GET","urlExpr":"'http://localhost'"},"assertions":["1==1"]}
                  ]
                }
                """;
        var parsed = engine.parseCaseConfig(ConfigFormat.JSON, json);
        Assertions.assertNotNull(parsed.caseConfig());
        Assertions.assertTrue(parsed.normalizedDefinition().contains("\"caseCode\":\"C1\""));
    }

    @Test
    void shouldRejectYamlAnchorSyntax() {
        String yaml = """
                meta:
                  caseCode: C1
                  caseName: c1
                steps:
                  - &anchorStep
                    name: s1
                    request:
                      method: GET
                      urlExpr: "'http://localhost'"
                """;
        Assertions.assertThrows(BusinessException.class, () -> engine.parseCaseConfig(ConfigFormat.YAML, yaml));
    }

    @Test
    void shouldNormalizeYamlAndJsonToSameStructure() throws Exception {
        String json = """
                {
                  "meta": {"caseCode":"C1","caseName":"case1"},
                  "steps":[
                    {"name":"s1","request":{"method":"GET","urlExpr":"'http://localhost'"},"assertions":["status==200"]}
                  ]
                }
                """;
        String yaml = """
                meta:
                  caseCode: C1
                  caseName: case1
                steps:
                  - name: s1
                    request:
                      method: GET
                      urlExpr: "'http://localhost'"
                    assertions:
                      - "status==200"
                """;
        var normalizedJson = engine.parseCaseConfig(ConfigFormat.JSON, json).normalizedDefinition();
        var normalizedYaml = engine.parseCaseConfig(ConfigFormat.YAML, yaml).normalizedDefinition();
        ObjectMapper mapper = new ObjectMapper();
        Assertions.assertEquals(mapper.readTree(normalizedJson), mapper.readTree(normalizedYaml));
    }
}
