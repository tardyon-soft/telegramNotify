package ru.wildred.telegram.notifier.core.sender;

public class TelegramSendException extends RuntimeException {
    public TelegramSendException(String message) {
        super(message);
    }

    public TelegramSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
