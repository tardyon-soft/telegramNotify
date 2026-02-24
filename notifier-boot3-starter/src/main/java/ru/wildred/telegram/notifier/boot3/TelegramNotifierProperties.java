package ru.wildred.telegram.notifier.boot3;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.wildred.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.wildred.telegram.notifier.core.dispatch.ParseMode;

@ConfigurationProperties("telegram.notifier")
public class TelegramNotifierProperties {
    private boolean enabled = true;
    private String token;
    private String username;
    private List<Long> chatIds = new ArrayList<Long>();
    private boolean asyncEnabled;
    private BotProperties bot = new BotProperties();
    private TargetsProperties targets = new TargetsProperties();
    private AsyncProperties async = new AsyncProperties();
    private boolean disableWebPagePreview;
    private ParseMode parseMode = ParseMode.PLAIN;
    private ErrorPolicy errorPolicy = ErrorPolicy.LOG_ONLY;
    private int executorCorePoolSize = 1;
    private int executorMaxPoolSize = 1;
    private int executorQueueCapacity = 100;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getToken() {
        if (hasText(token)) {
            return token;
        }
        return bot != null ? bot.getToken() : null;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        if (hasText(username)) {
            return username;
        }
        return bot != null ? bot.getUsername() : null;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Long> getChatIds() {
        if (chatIds != null && !chatIds.isEmpty()) {
            return chatIds;
        }
        if (targets != null && targets.getChatIds() != null) {
            return targets.getChatIds();
        }
        return chatIds;
    }

    public void setChatIds(List<Long> chatIds) {
        this.chatIds = chatIds;
    }

    public boolean isAsyncEnabled() {
        if (asyncEnabled) {
            return true;
        }
        if (async != null) {
            return async.isEnabled();
        }
        return asyncEnabled;
    }

    public void setAsyncEnabled(boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }

    public BotProperties getBot() {
        return bot;
    }

    public void setBot(BotProperties bot) {
        this.bot = bot;
    }

    public TargetsProperties getTargets() {
        return targets;
    }

    public void setTargets(TargetsProperties targets) {
        this.targets = targets;
    }

    public AsyncProperties getAsync() {
        return async;
    }

    public void setAsync(AsyncProperties async) {
        this.async = async;
    }

    public boolean isDisableWebPagePreview() {
        return disableWebPagePreview;
    }

    public void setDisableWebPagePreview(boolean disableWebPagePreview) {
        this.disableWebPagePreview = disableWebPagePreview;
    }

    public ParseMode getParseMode() {
        return parseMode;
    }

    public void setParseMode(ParseMode parseMode) {
        this.parseMode = parseMode;
    }

    public ErrorPolicy getErrorPolicy() {
        return errorPolicy;
    }

    public void setErrorPolicy(ErrorPolicy errorPolicy) {
        this.errorPolicy = errorPolicy;
    }

    public int getExecutorCorePoolSize() {
        return executorCorePoolSize;
    }

    public void setExecutorCorePoolSize(int executorCorePoolSize) {
        this.executorCorePoolSize = executorCorePoolSize;
    }

    public int getExecutorMaxPoolSize() {
        return executorMaxPoolSize;
    }

    public void setExecutorMaxPoolSize(int executorMaxPoolSize) {
        this.executorMaxPoolSize = executorMaxPoolSize;
    }

    public int getExecutorQueueCapacity() {
        return executorQueueCapacity;
    }

    public void setExecutorQueueCapacity(int executorQueueCapacity) {
        this.executorQueueCapacity = executorQueueCapacity;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static class BotProperties {
        private String token;
        private String username;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class TargetsProperties {
        private List<Long> chatIds = new ArrayList<Long>();

        public List<Long> getChatIds() {
            return chatIds;
        }

        public void setChatIds(List<Long> chatIds) {
            this.chatIds = chatIds;
        }
    }

    public static class AsyncProperties {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
