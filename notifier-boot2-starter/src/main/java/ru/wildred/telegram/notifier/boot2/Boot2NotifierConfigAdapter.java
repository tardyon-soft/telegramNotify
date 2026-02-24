package ru.wildred.telegram.notifier.boot2;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import ru.wildred.telegram.notifier.core.config.NotifierConfig;
import ru.wildred.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.wildred.telegram.notifier.core.dispatch.ParseMode;

public class Boot2NotifierConfigAdapter implements NotifierConfig {
    private final TelegramNotifierProperties properties;
    private final Executor asyncExecutor;

    public Boot2NotifierConfigAdapter(TelegramNotifierProperties properties, Executor asyncExecutor) {
        this.properties = properties;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public List<Long> defaultChatIds() {
        List<Long> chatIds = properties.getChatIds();
        if (chatIds == null) {
            return Collections.emptyList();
        }
        return chatIds;
    }

    @Override
    public String botToken() {
        return properties.getToken();
    }

    @Override
    public boolean disableWebPagePreview() {
        return properties.isDisableWebPagePreview();
    }

    @Override
    public boolean asyncEnabled() {
        return properties.isAsyncEnabled();
    }

    @Override
    public Executor asyncExecutor() {
        return asyncExecutor;
    }

    @Override
    public ParseMode parseMode() {
        return properties.getParseMode();
    }

    @Override
    public ErrorPolicy errorPolicy() {
        return properties.getErrorPolicy();
    }
}
