package ru.wildred.telegram.notifier.core.dispatch;

import java.util.List;

public final class NotificationOptions {
    private final List<Long> chatIdsOverride;
    private final ParseMode parseMode;
    private final ErrorPolicy errorPolicy;

    public NotificationOptions(List<Long> chatIdsOverride, ParseMode parseMode, ErrorPolicy errorPolicy) {
        this.chatIdsOverride = chatIdsOverride;
        this.parseMode = parseMode;
        this.errorPolicy = errorPolicy;
    }

    public List<Long> chatIdsOverride() {
        return chatIdsOverride;
    }

    public ParseMode parseMode() {
        return parseMode;
    }

    public ErrorPolicy errorPolicy() {
        return errorPolicy;
    }
}
