package ru.wildred.telegram.notifier.spel;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import ru.wildred.telegram.notifier.core.aop.MethodInvocationContext;
import ru.wildred.telegram.notifier.core.template.TemplateEngine;

public class DefaultSpelTemplateEngine implements TemplateEngine {
    private final ExpressionParser expressionParser;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final ConcurrentMap<String, Expression> expressionCache;

    public DefaultSpelTemplateEngine() {
        this(new SpelExpressionParser(), new DefaultParameterNameDiscoverer(), new ConcurrentHashMap<String, Expression>());
    }

    DefaultSpelTemplateEngine(
            ExpressionParser expressionParser,
            ParameterNameDiscoverer parameterNameDiscoverer,
            ConcurrentMap<String, Expression> expressionCache
    ) {
        this.expressionParser = Objects.requireNonNull(expressionParser, "expressionParser");
        this.parameterNameDiscoverer = Objects.requireNonNull(parameterNameDiscoverer, "parameterNameDiscoverer");
        this.expressionCache = Objects.requireNonNull(expressionCache, "expressionCache");
    }

    @Override
    public String render(String template, MethodInvocationContext context) {
        if (isBlank(template)) {
            return "";
        }

        Object value = evaluateInternal(template, safeContext(context));
        return value == null ? "" : String.valueOf(value);
    }

    public boolean evaluateCondition(String conditionTemplate, MethodInvocationContext context) {
        if (isBlank(conditionTemplate)) {
            return true;
        }

        Object value = evaluateInternal(conditionTemplate, safeContext(context));
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value).trim());
    }

    private Object evaluateInternal(String expression, MethodInvocationContext context) {
        Expression parsedExpression = expressionCache.computeIfAbsent(expression, expressionParser::parseExpression);
        return parsedExpression.getValue(createEvaluationContext(context));
    }

    private EvaluationContext createEvaluationContext(MethodInvocationContext context) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

        Object[] args = context.arguments();
        evaluationContext.setVariable("args", args);
        for (int i = 0; i < args.length; i++) {
            evaluationContext.setVariable("p" + i, args[i]);
            evaluationContext.setVariable("a" + i, args[i]);
        }

        evaluationContext.setVariable("result", context.result());
        evaluationContext.setVariable("ex", context.throwable());

        Method method = context.method();
        if (method != null) {
            evaluationContext.setVariable("methodName", method.getName());
            evaluationContext.setVariable("className", method.getDeclaringClass().getName());

            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
            if (parameterNames != null) {
                int limit = Math.min(parameterNames.length, args.length);
                for (int i = 0; i < limit; i++) {
                    evaluationContext.setVariable(parameterNames[i], args[i]);
                }
            }
        } else {
            evaluationContext.setVariable("methodName", null);
            evaluationContext.setVariable("className",
                    context.target() == null ? null : context.target().getClass().getName());
        }

        return evaluationContext;
    }

    private MethodInvocationContext safeContext(MethodInvocationContext context) {
        return context == null ? MethodInvocationContext.empty() : context;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
