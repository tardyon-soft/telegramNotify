package ru.wildred.telegram.notifier.core.template;

import ru.wildred.telegram.notifier.core.aop.MethodInvocationContext;

public interface TemplateEngine {
    String render(String template, MethodInvocationContext context);
}
