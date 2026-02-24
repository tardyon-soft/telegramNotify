package ru.wildred.telegram.notifier.core.dispatch;

import java.util.List;
import ru.wildred.telegram.notifier.core.aop.MethodInvocationContext;

public final class NotificationRequest {
    private final String messageTemplate;
    private final String conditionTemplate;
    private final NotificationOptions options;
    private final MethodInvocationContext context;

    public NotificationRequest(
            String messageTemplate,
            String conditionTemplate,
            NotificationOptions options,
            MethodInvocationContext context
    ) {
        if (messageTemplate == null) {
            throw new IllegalArgumentException("messageTemplate must not be null");
        }
        this.messageTemplate = messageTemplate;
        this.conditionTemplate = conditionTemplate;
        this.options = options;
        this.context = context == null ? MethodInvocationContext.empty() : context;
    }

    public static NotificationRequest of(String messageTemplate, String conditionTemplate) {
        return new NotificationRequest(messageTemplate, conditionTemplate, null, MethodInvocationContext.empty());
    }

    public static NotificationRequest of(
            String messageTemplate,
            String conditionTemplate,
            List<Long> chatIdsOverride,
            ParseMode parseMode,
            ErrorPolicy errorPolicy,
            MethodInvocationContext context
    ) {
        return new NotificationRequest(
                messageTemplate,
                conditionTemplate,
                new NotificationOptions(chatIdsOverride, parseMode, errorPolicy),
                context
        );
    }

    public String messageTemplate() {
        return messageTemplate;
    }

    public String conditionTemplate() {
        return conditionTemplate;
    }

    public NotificationOptions options() {
        return options;
    }

    public MethodInvocationContext context() {
        return context;
    }
}
