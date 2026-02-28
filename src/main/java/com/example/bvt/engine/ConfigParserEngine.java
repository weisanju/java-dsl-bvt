package com.example.bvt.engine;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.enums.ConfigFormat;
import com.example.bvt.engine.model.CaseConfig;
import com.example.bvt.engine.model.ParsedCaseConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class ConfigParserEngine {

    private static final Pattern YAML_ANCHOR_PATTERN = Pattern.compile("(?m)^\\s*[^#\\n]*&[A-Za-z0-9_-]+");
    private static final Pattern YAML_ALIAS_PATTERN = Pattern.compile("(?m)^\\s*[^#\\n]*\\*[A-Za-z0-9_-]+");

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public ConfigParserEngine(ObjectMapper jsonMapper, @Qualifier("yamlMapper") ObjectMapper yamlMapper) {
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
    }

    public ParsedCaseConfig parseCaseConfig(ConfigFormat format, String content) {
        JsonNode root = parseContent(format, content);
        validateCaseConfig(root);
        try {
            String normalized = jsonMapper.writeValueAsString(root);
            CaseConfig caseConfig = jsonMapper.treeToValue(root, CaseConfig.class);
            return new ParsedCaseConfig(normalized, caseConfig);
        } catch (Exception ex) {
            throw new BusinessException("Normalize case config failed: " + ex.getMessage());
        }
    }

    public CaseConfig parseNormalizedCaseConfig(String normalizedDefinition) {
        try {
            JsonNode root = jsonMapper.readTree(normalizedDefinition);
            validateCaseConfig(root);
            return jsonMapper.treeToValue(root, CaseConfig.class);
        } catch (Exception ex) {
            throw new BusinessException("Parse normalized config failed: " + ex.getMessage());
        }
    }

    public Map<String, Object> parseDataContent(ConfigFormat format, String content) {
        JsonNode root = parseContent(format, content);
        try {
            return jsonMapper.convertValue(root, Map.class);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Data set must be object-like content");
        }
    }

    private JsonNode parseContent(ConfigFormat format, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("Config content cannot be empty");
        }
        try {
            if (format == ConfigFormat.JSON) {
                return jsonMapper.readTree(content);
            }
            validateYamlRestrictions(content);
            return yamlMapper.readTree(content);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Parse config failed: " + ex.getMessage());
        }
    }

    private void validateYamlRestrictions(String content) {
        if (content.contains("\n---") || content.stripLeading().startsWith("---")) {
            throw new BusinessException("YAML multi-document syntax is not allowed");
        }
        if (YAML_ANCHOR_PATTERN.matcher(content).find()) {
            throw new BusinessException("YAML anchor syntax is not allowed");
        }
        if (YAML_ALIAS_PATTERN.matcher(content).find()) {
            throw new BusinessException("YAML alias syntax is not allowed");
        }
    }

    private void validateCaseConfig(JsonNode root) {
        if (root == null || !root.isObject()) {
            throw new BusinessException("Case config must be object");
        }
        JsonNode meta = root.get("meta");
        if (meta == null || !meta.isObject()) {
            throw new BusinessException("meta is required");
        }
        requireText(meta, "caseCode");
        requireText(meta, "caseName");

        JsonNode steps = root.get("steps");
        if (steps == null || !steps.isArray() || steps.isEmpty()) {
            throw new BusinessException("steps must be non-empty array");
        }
        int index = 0;
        for (JsonNode step : steps) {
            if (!step.isObject()) {
                throw new BusinessException("step[" + index + "] must be object");
            }
            requireText(step, "name");
            JsonNode request = step.get("request");
            if (request == null || !request.isObject()) {
                throw new BusinessException("step[" + index + "].request is required");
            }
            requireText(request, "method");
            requireText(request, "urlExpr");
            validateAssertions(step, index);
            validateExtracts(step, index);
            validateOptionalText(step, "skipWhen", "step[" + index + "].skipWhen");
            index++;
        }
    }

    private void validateAssertions(JsonNode step, int index) {
        JsonNode assertions = step.get("assertions");
        if (assertions == null || assertions.isNull()) {
            return;
        }
        if (!assertions.isArray()) {
            throw new BusinessException("step[" + index + "].assertions must be array");
        }
        for (JsonNode assertion : assertions) {
            if (!assertion.isTextual()) {
                throw new BusinessException("step[" + index + "].assertions must be string array");
            }
        }
    }

    private void validateExtracts(JsonNode step, int index) {
        JsonNode extracts = step.get("extracts");
        if (extracts == null || extracts.isNull()) {
            return;
        }
        if (!extracts.isObject()) {
            throw new BusinessException("step[" + index + "].extracts must be object");
        }
        Iterator<Map.Entry<String, JsonNode>> fields = extracts.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (!field.getValue().isTextual()) {
                throw new BusinessException("step[" + index + "].extracts value must be string");
            }
        }
    }

    private void requireText(JsonNode node, String field) {
        JsonNode child = node.get(field);
        if (child == null || !child.isTextual() || child.asText().isBlank()) {
            throw new BusinessException(field + " is required");
        }
    }

    private void validateOptionalText(JsonNode node, String field, String messagePrefix) {
        JsonNode child = node.get(field);
        if (child != null && !child.isNull() && !child.isTextual()) {
            throw new BusinessException(messagePrefix + " must be string");
        }
    }
}
