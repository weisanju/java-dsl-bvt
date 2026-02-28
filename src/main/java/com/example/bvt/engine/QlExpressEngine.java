package com.example.bvt.engine;

import com.example.bvt.common.BusinessException;
import com.jayway.jsonpath.JsonPath;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class QlExpressEngine {

    private final ExpressRunner runner;
    private final ExpressionSecurityValidator securityValidator;

    public QlExpressEngine(ExpressionSecurityValidator securityValidator) {
        this.securityValidator = securityValidator;
        this.runner = new ExpressRunner();
        QlFunctions functions = new QlFunctions();
        try {
            runner.addFunctionOfServiceMethod("jsonPath", functions, "jsonPath",
                    new Class[]{Object.class, String.class}, null);
            runner.addFunctionOfServiceMethod("len", functions, "len",
                    new Class[]{Object.class}, null);
            runner.addFunctionOfServiceMethod("contains", functions, "contains",
                    new Class[]{Object.class, Object.class}, null);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to register QLExpress functions", ex);
        }
    }

    public Object execute(String expression, Map<String, Object> context) {
        securityValidator.validate(expression);
        try {
            DefaultContext<String, Object> qlContext = new DefaultContext<>();
            qlContext.putAll(context);
            return runner.execute(expression, qlContext, null, true, false);
        } catch (Exception ex) {
            throw new BusinessException("Expression execute error: " + ex.getMessage());
        }
    }

    public boolean evalBoolean(String expression, Map<String, Object> context) {
        Object result = execute(expression, context);
        if (result instanceof Boolean bool) {
            return bool;
        }
        return result != null && Boolean.parseBoolean(String.valueOf(result));
    }

    static class QlFunctions {

        public Object jsonPath(Object body, String path) {
            return JsonPath.read(body, path);
        }

        public int len(Object value) {
            if (value == null) {
                return 0;
            }
            if (value instanceof CharSequence cs) {
                return cs.length();
            }
            if (value instanceof Collection<?> collection) {
                return collection.size();
            }
            if (value instanceof Map<?, ?> map) {
                return map.size();
            }
            return 0;
        }

        public boolean contains(Object source, Object target) {
            if (source == null) {
                return false;
            }
            if (source instanceof Collection<?> collection) {
                return collection.contains(target);
            }
            if (source instanceof String str && target != null) {
                return str.contains(String.valueOf(target));
            }
            return false;
        }
    }
}
