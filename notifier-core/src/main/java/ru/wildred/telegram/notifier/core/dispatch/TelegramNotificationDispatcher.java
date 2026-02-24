package ru.wildred.telegram.notifier.core.dispatch;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.wildred.telegram.notifier.core.aop.MethodInvocationContext;
import ru.wildred.telegram.notifier.core.config.NotifierConfig;
import ru.wildred.telegram.notifier.core.sender.TelegramSender;
import ru.wildred.telegram.notifier.core.template.TemplateEngine;

public class TelegramNotificationDispatcher {
    private static final Logger LOGGER = Logger.getLogger(TelegramNotificationDispatcher.class.getName());

    private final NotifierConfig config;
    private final TemplateEngine templateEngine;
    private final TelegramSender telegramSender;
    private final Executor executor;

    public TelegramNotificationDispatcher(
            NotifierConfig config,
            TemplateEngine templateEngine,
            TelegramSender telegramSender,
            Executor executor
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.templateEngine = Objects.requireNonNull(templateEngine, "templateEngine");
        this.telegramSender = Objects.requireNonNull(telegramSender, "telegramSender");
        this.executor = executor;
    }

    public void dispatch(NotificationRequest request) {
        Objects.requireNonNull(request, "request");

        MethodInvocationContext context = request.context() == null ? MethodInvocationContext.empty() : request.context();

        if (!evaluateCondition(request.conditionTemplate(), context)) {
            return;
        }

        String message = templateEngine.render(request.messageTemplate(), context);
        if (message == null || message.isBlank()) {
            return;
        }

        List<Long> chatIds = resolveChatIds(request.options());
        if (chatIds.isEmpty()) {
            return;
        }

        ParseMode parseMode = resolveParseMode(request.options());
        ErrorPolicy errorPolicy = resolveErrorPolicy(request.options());

        Runnable sendTask = () -> {
            for (Long chatId : chatIds) {
                sendSingle(chatId, message, parseMode, errorPolicy);
            }
        };

        Executor effectiveExecutor = executor != null ? executor : config.asyncExecutor();
        if (config.asyncEnabled() && effectiveExecutor != null) {
            effectiveExecutor.execute(sendTask);
            return;
        }

        sendTask.run();
    }

    private boolean evaluateCondition(String conditionTemplate, MethodInvocationContext context) {
        if (conditionTemplate == null || conditionTemplate.isBlank()) {
            return true;
        }
        String rendered = templateEngine.render(conditionTemplate, context);
        if (rendered == null) {
            return false;
        }
        return Boolean.parseBoolean(rendered.trim().toLowerCase(Locale.ROOT));
    }

    private List<Long> resolveChatIds(NotificationOptions options) {
        if (options != null && options.chatIdsOverride() != null && !options.chatIdsOverride().isEmpty()) {
            return List.copyOf(options.chatIdsOverride());
        }

        List<Long> defaults = config.defaultChatIds();
        if (defaults == null || defaults.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(defaults);
    }

    private ParseMode resolveParseMode(NotificationOptions options) {
        if (options != null && options.parseMode() != null) {
            return options.parseMode();
        }
        return config.parseMode();
    }

    private ErrorPolicy resolveErrorPolicy(NotificationOptions options) {
        if (options != null && options.errorPolicy() != null) {
            return options.errorPolicy();
        }
        return config.errorPolicy();
    }

    private void sendSingle(Long chatId, String message, ParseMode parseMode, ErrorPolicy errorPolicy) {
        try {
            telegramSender.send(chatId, message, parseMode);
        } catch (RuntimeException ex) {
            if (errorPolicy == ErrorPolicy.THROW) {
                throw ex;
            }
            LOGGER.log(Level.WARNING, "Failed to send telegram notification to chatId=" + chatId, ex);
        }
    }
}
