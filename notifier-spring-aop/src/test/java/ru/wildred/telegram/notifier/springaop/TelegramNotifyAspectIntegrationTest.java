package ru.wildred.telegram.notifier.springaop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ru.wildred.telegram.notifier.core.annotation.NotifyWhen;
import ru.wildred.telegram.notifier.core.annotation.TelegramNotify;
import ru.wildred.telegram.notifier.core.aop.MethodInvocationContext;
import ru.wildred.telegram.notifier.core.config.NotifierConfig;
import ru.wildred.telegram.notifier.core.dispatch.TelegramNotificationDispatcher;
import ru.wildred.telegram.notifier.core.sender.TelegramSender;
import ru.wildred.telegram.notifier.core.template.TemplateEngine;

class TelegramNotifyAspectIntegrationTest {

    @Test
    void senderIsCalledOnSuccessAndFailure() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class);
        try {
            SampleService service = context.getBean(SampleService.class);
            RecordingSender sender = context.getBean(RecordingSender.class);

            service.success("ok");
            assertThrows(IllegalStateException.class, () -> service.failure("bad"));

            assertEquals(2, sender.sentMessages().size());
            assertEquals("success-ok", sender.sentMessages().get(0));
            assertEquals("failure-bad", sender.sentMessages().get(1));
        } finally {
            context.close();
        }
    }

    interface SampleService {
        String success(String value);

        String failure(String value);
    }

    static class SampleServiceImpl implements SampleService {
        @Override
        @TelegramNotify(message = "'success-' + #p0", when = NotifyWhen.AFTER_SUCCESS, chatIds = {1001L})
        public String success(String value) {
            return "done-" + value;
        }

        @Override
        @TelegramNotify(message = "'failure-' + #p0", when = NotifyWhen.AFTER_FAILURE, chatIds = {1001L})
        public String failure(String value) {
            throw new IllegalStateException("boom");
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        RecordingSender recordingSender() {
            return new RecordingSender();
        }

        @Bean
        TemplateEngine templateEngine() {
            return new TemplateEngine() {
                @Override
                public String render(String template, MethodInvocationContext context) {
                    if (template == null || template.trim().isEmpty()) {
                        return "";
                    }
                    if ("'success-' + #p0".equals(template)) {
                        return "success-" + String.valueOf(context.arguments()[0]);
                    }
                    if ("'failure-' + #p0".equals(template)) {
                        return "failure-" + String.valueOf(context.arguments()[0]);
                    }
                    return template;
                }
            };
        }

        @Bean
        NotifierConfig notifierConfig() {
            return new NotifierConfig() {
                @Override
                public List<Long> defaultChatIds() {
                    return Collections.singletonList(9999L);
                }
            };
        }

        @Bean
        TelegramNotificationDispatcher telegramNotificationDispatcher(
                NotifierConfig notifierConfig,
                TemplateEngine templateEngine,
                RecordingSender sender
        ) {
            return new TelegramNotificationDispatcher(notifierConfig, templateEngine, sender, null);
        }

        @Bean
        TelegramNotifyAspect telegramNotifyAspect(TelegramNotificationDispatcher dispatcher) {
            return new TelegramNotifyAspect(dispatcher);
        }

        @Bean
        SampleService sampleService() {
            return new SampleServiceImpl();
        }
    }

    static class RecordingSender implements TelegramSender {
        private final List<String> sentMessages = new ArrayList<String>();

        @Override
        public void send(long chatId, String message, ru.wildred.telegram.notifier.core.dispatch.ParseMode parseMode) {
            sentMessages.add(message);
        }

        List<String> sentMessages() {
            return sentMessages;
        }
    }
}
