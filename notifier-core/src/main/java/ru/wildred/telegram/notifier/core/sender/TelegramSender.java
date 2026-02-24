package ru.wildred.telegram.notifier.core.sender;

import ru.wildred.telegram.notifier.core.dispatch.ParseMode;

public interface TelegramSender {
    void send(long chatId, String message, ParseMode parseMode) throws TelegramSendException;
}
