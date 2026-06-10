package ru.tardyon.maven.telegram.notifier.telegram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tardyon.maven.telegram.notifier.core.config.NotifierConfig;
import ru.tardyon.maven.telegram.notifier.core.config.ProxyType;
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

    @Test
    void createsHttpProxyOptions() {
        DefaultBotOptions options = DefaultTelegramSender.createBotOptions(proxyConfig(ProxyType.HTTP, "127.0.0.1", 8080));

        assertEquals(DefaultBotOptions.ProxyType.HTTP, options.getProxyType());
        assertEquals("127.0.0.1", options.getProxyHost());
        assertEquals(8080, options.getProxyPort());
    }

    @Test
    void createsSocks5ProxyOptions() {
        DefaultBotOptions options = DefaultTelegramSender.createBotOptions(proxyConfig(ProxyType.SOCKS5, "10.0.0.2", 1080));

        assertEquals(DefaultBotOptions.ProxyType.SOCKS5, options.getProxyType());
        assertEquals("10.0.0.2", options.getProxyHost());
        assertEquals(1080, options.getProxyPort());
    }

    @Test
    void keepsNoProxyWhenConfigIncomplete() {
        DefaultBotOptions disabled = DefaultTelegramSender.createBotOptions(config(false));
        assertEquals(DefaultBotOptions.ProxyType.NO_PROXY, disabled.getProxyType());

        DefaultBotOptions missingHost = DefaultTelegramSender.createBotOptions(proxyConfig(ProxyType.HTTP, "", 8080));
        assertEquals(DefaultBotOptions.ProxyType.NO_PROXY, missingHost.getProxyType());

        DefaultBotOptions missingPort = DefaultTelegramSender.createBotOptions(proxyConfig(ProxyType.HTTP, "127.0.0.1", 0));
        assertEquals(DefaultBotOptions.ProxyType.NO_PROXY, missingPort.getProxyType());
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

    private static NotifierConfig proxyConfig(ProxyType proxyType, String host, int port) {
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
            public boolean proxyEnabled() {
                return true;
            }

            @Override
            public ProxyType proxyType() {
                return proxyType;
            }

            @Override
            public String proxyHost() {
                return host;
            }

            @Override
            public int proxyPort() {
                return port;
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
