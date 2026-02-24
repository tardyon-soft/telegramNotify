package ru.tardyon.maven.telegram.notifier.telegram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tardyon.maven.telegram.notifier.core.config.NotifierConfig;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

class DefaultTelegramSenderTest {

    @Test
    void appliesHtmlParseModeAndDisablePreview() {
        CapturingExecutor executor = new CapturingExecutor();
        DefaultTelegramSender sender = new DefaultTelegramSender(config(true), executor);

        sender.send(123L, "hello", ParseMode.HTML);

        assertEquals("123", executor.lastSendMessage.getChatId());
        assertEquals("hello", executor.lastSendMessage.getText());
        assertEquals(org.telegram.telegrambots.meta.api.methods.ParseMode.HTML, executor.lastSendMessage.getParseMode());
        assertTrue(Boolean.TRUE.equals(executor.lastSendMessage.getDisableWebPagePreview()));
    }

    @Test
    void appliesMarkdownVariantsAndPlainMode() {
        CapturingExecutor executor = new CapturingExecutor();
        DefaultTelegramSender sender = new DefaultTelegramSender(config(false), executor);

        sender.send(1L, "m1", ParseMode.MARKDOWN);
        assertEquals(org.telegram.telegrambots.meta.api.methods.ParseMode.MARKDOWN, executor.lastSendMessage.getParseMode());
        assertFalse(Boolean.TRUE.equals(executor.lastSendMessage.getDisableWebPagePreview()));

        sender.send(1L, "m2", ParseMode.MARKDOWN_V2);
        assertEquals(org.telegram.telegrambots.meta.api.methods.ParseMode.MARKDOWNV2, executor.lastSendMessage.getParseMode());

        sender.send(1L, "m3", ParseMode.PLAIN);
        assertNull(executor.lastSendMessage.getParseMode());
    }

    private static NotifierConfig config(boolean disablePreview) {
        return new NotifierConfig() {
            @Override
            public java.util.List<Long> defaultChatIds() {
                return java.util.Collections.emptyList();
            }

            @Override
            public String botToken() {
                return "token";
            }

            @Override
            public boolean disableWebPagePreview() {
                return disablePreview;
            }
        };
    }

    private static final class CapturingExecutor implements DefaultTelegramSender.TelegramRequestExecutor {
        private SendMessage lastSendMessage;

        @Override
        public void execute(SendMessage sendMessage) throws TelegramApiException {
            this.lastSendMessage = sendMessage;
        }
    }
}
