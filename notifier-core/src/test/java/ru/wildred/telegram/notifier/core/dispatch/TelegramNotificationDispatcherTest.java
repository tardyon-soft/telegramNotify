package ru.wildred.telegram.notifier.core.dispatch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import ru.wildred.telegram.notifier.core.aop.MethodInvocationContext;
import ru.wildred.telegram.notifier.core.config.NotifierConfig;
import ru.wildred.telegram.notifier.core.sender.TelegramSendException;
import ru.wildred.telegram.notifier.core.sender.TelegramSender;
import ru.wildred.telegram.notifier.core.template.TemplateEngine;

class TelegramNotificationDispatcherTest {

    @Test
    void conditionFalseDoesNotInvokeSender() {
        RecordingSender sender = new RecordingSender();
        TelegramNotificationDispatcher dispatcher = new TelegramNotificationDispatcher(
                config(List.of(1001L), false, null),
                templateEngine(Map.of("cond", "false", "msg", "Hello")),
                sender,
                null
        );

        NotificationRequest request = NotificationRequest.of(
                "msg",
                "cond",
                null,
                ParseMode.PLAIN,
                ErrorPolicy.THROW,
                MethodInvocationContext.empty()
        );

        dispatcher.dispatch(request);

        assertTrue(sender.calls.isEmpty());
    }

    @Test
    void emptyMessageDoesNotInvokeSender() {
        RecordingSender sender = new RecordingSender();
        TelegramNotificationDispatcher dispatcher = new TelegramNotificationDispatcher(
                config(List.of(1001L), false, null),
                templateEngine(Map.of("cond", "true", "msg", "   ")),
                sender,
                null
        );

        NotificationRequest request = NotificationRequest.of(
                "msg",
                "cond",
                null,
                ParseMode.PLAIN,
                ErrorPolicy.THROW,
                MethodInvocationContext.empty()
        );

        dispatcher.dispatch(request);

        assertTrue(sender.calls.isEmpty());
    }

    @Test
    void overrideChatIdsAreUsedInsteadOfDefault() {
        RecordingSender sender = new RecordingSender();
        TelegramNotificationDispatcher dispatcher = new TelegramNotificationDispatcher(
                config(List.of(1L, 2L), false, null),
                templateEngine(Map.of("cond", "true", "msg", "Hello")),
                sender,
                null
        );

        NotificationRequest request = NotificationRequest.of(
                "msg",
                "cond",
                List.of(9L, 10L),
                ParseMode.PLAIN,
                ErrorPolicy.THROW,
                MethodInvocationContext.empty()
        );

        dispatcher.dispatch(request);

        assertEquals(List.of(9L, 10L), sender.sentChatIds());
    }

    @Test
    void logOnlyPolicySwallowsSenderException() {
        TelegramSender failingSender = (chatId, message, parseMode) -> {
            throw new TelegramSendException("boom");
        };
        TelegramNotificationDispatcher dispatcher = new TelegramNotificationDispatcher(
                config(List.of(42L), false, null),
                templateEngine(Map.of("cond", "true", "msg", "Hello")),
                failingSender,
                null
        );

        NotificationRequest request = NotificationRequest.of(
                "msg",
                "cond",
                null,
                ParseMode.PLAIN,
                ErrorPolicy.LOG_ONLY,
                MethodInvocationContext.empty()
        );

        assertDoesNotThrow(() -> dispatcher.dispatch(request));
    }

    private static TemplateEngine templateEngine(Map<String, String> renderedValues) {
        return (template, context) -> renderedValues.getOrDefault(template, template);
    }

    private static NotifierConfig config(List<Long> chatIds, boolean asyncEnabled, Executor asyncExecutor) {
        return new NotifierConfig() {
            @Override
            public List<Long> defaultChatIds() {
                return chatIds;
            }

            @Override
            public boolean asyncEnabled() {
                return asyncEnabled;
            }

            @Override
            public Executor asyncExecutor() {
                return asyncExecutor;
            }
        };
    }

    private static final class RecordingSender implements TelegramSender {
        private final List<SendCall> calls = new ArrayList<>();

        @Override
        public void send(long chatId, String message, ParseMode parseMode) {
            calls.add(new SendCall(chatId, message, parseMode));
        }

        private List<Long> sentChatIds() {
            return calls.stream().map(SendCall::chatId).collect(Collectors.toList());
        }
    }

    private static final class SendCall {
        private final long chatId;
        private final String message;
        private final ParseMode parseMode;

        private SendCall(long chatId, String message, ParseMode parseMode) {
            this.chatId = chatId;
            this.message = message;
            this.parseMode = parseMode;
        }

        private long chatId() {
            return chatId;
        }

        private String message() {
            return message;
        }

        private ParseMode parseMode() {
            return parseMode;
        }
    }
}
