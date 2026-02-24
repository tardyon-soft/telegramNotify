package ru.wildred.telegram.notifier.core.config;

import java.util.List;
import java.util.concurrent.Executor;
import ru.wildred.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.wildred.telegram.notifier.core.dispatch.ParseMode;

public interface NotifierConfig {
    List<Long> defaultChatIds();

    default String botToken() {
        return "";
    }

    default boolean disableWebPagePreview() {
        return false;
    }

    default boolean asyncEnabled() {
        return false;
    }

    default Executor asyncExecutor() {
        return null;
    }

    default ParseMode parseMode() {
        return ParseMode.PLAIN;
    }

    default ErrorPolicy errorPolicy() {
        return ErrorPolicy.LOG_ONLY;
    }
}
