package ru.wildred.telegram.notifier.telegram;

import java.util.List;
import java.util.Objects;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.wildred.telegram.notifier.core.config.NotifierConfig;
import ru.wildred.telegram.notifier.core.dispatch.ParseMode;
import ru.wildred.telegram.notifier.core.sender.TelegramSendException;
import ru.wildred.telegram.notifier.core.sender.TelegramSender;

public class DefaultTelegramSender implements TelegramSender {
    private final NotifierConfig config;
    private final TelegramRequestExecutor requestExecutor;

    public DefaultTelegramSender(NotifierConfig config) {
        this(config, createDefaultAbsSender(config));
    }

    public DefaultTelegramSender(NotifierConfig config, DefaultAbsSender sender) {
        this(config, new DefaultAbsSenderRequestExecutor(sender));
    }

    public DefaultTelegramSender(NotifierConfig config, TelegramRequestExecutor requestExecutor) {
        this.config = Objects.requireNonNull(config, "config");
        this.requestExecutor = Objects.requireNonNull(requestExecutor, "requestExecutor");
    }

    @Override
    public void send(long chatId, String message, ParseMode parseMode) throws TelegramSendException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        sendMessage.setDisableWebPagePreview(config.disableWebPagePreview());

        String parseModeValue = mapParseMode(parseMode);
        if (parseModeValue != null) {
            sendMessage.setParseMode(parseModeValue);
        }

        try {
            requestExecutor.execute(sendMessage);
        } catch (TelegramApiException ex) {
            throw new TelegramSendException("Failed to send telegram message", ex);
        }
    }

    public void sendMany(List<Long> chatIds, String message, ParseMode parseMode) throws TelegramSendException {
        if (chatIds == null || chatIds.isEmpty()) {
            return;
        }
        for (Long chatId : chatIds) {
            if (chatId != null) {
                send(chatId.longValue(), message, parseMode);
            }
        }
    }

    private static DefaultAbsSender createDefaultAbsSender(NotifierConfig config) {
        Objects.requireNonNull(config, "config");
        final String token = config.botToken();
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("botToken must not be blank");
        }
        return new DefaultAbsSender(new DefaultBotOptions()) {
            @Override
            public String getBotToken() {
                return token;
            }
        };
    }

    private String mapParseMode(ParseMode parseMode) {
        if (parseMode == null || parseMode == ParseMode.PLAIN) {
            return null;
        }
        if (parseMode == ParseMode.HTML) {
            return org.telegram.telegrambots.meta.api.methods.ParseMode.HTML;
        }
        if (parseMode == ParseMode.MARKDOWN_V2) {
            return org.telegram.telegrambots.meta.api.methods.ParseMode.MARKDOWNV2;
        }
        return org.telegram.telegrambots.meta.api.methods.ParseMode.MARKDOWN;
    }

    public interface TelegramRequestExecutor {
        void execute(SendMessage sendMessage) throws TelegramApiException;
    }

    private static final class DefaultAbsSenderRequestExecutor implements TelegramRequestExecutor {
        private final DefaultAbsSender sender;

        private DefaultAbsSenderRequestExecutor(DefaultAbsSender sender) {
            this.sender = Objects.requireNonNull(sender, "sender");
        }

        @Override
        public void execute(SendMessage sendMessage) throws TelegramApiException {
            sender.execute(sendMessage);
        }
    }
}
