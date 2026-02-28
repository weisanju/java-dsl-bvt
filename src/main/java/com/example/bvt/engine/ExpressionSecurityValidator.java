package com.example.bvt.engine;

import com.example.bvt.common.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class ExpressionSecurityValidator {

    private static final List<String> FORBIDDEN_TOKENS = List.of(
            "class",
            "import",
            "new ",
            "runtime",
            "system",
            "processbuilder",
            "getclass",
            "class.forname",
            "java.",
            "com."
    );

    public void validate(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new BusinessException("Expression cannot be empty");
        }
        String normalized = expression.toLowerCase(Locale.ROOT);
        for (String token : FORBIDDEN_TOKENS) {
            if (normalized.contains(token)) {
                throw new BusinessException("Forbidden token in expression: " + token);
            }
        }
    }
}
