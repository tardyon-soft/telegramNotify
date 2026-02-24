package ru.tardyon.maven.telegram.notifier.boot2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramNotifierAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TelegramNotifierAutoConfiguration.class));

    @Test
    void aspectIsMissingWithoutRequiredProperties() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean("telegramNotifyAspect"));
    }

    @Test
    void aspectIsPresentWithRequiredProperties() {
        contextRunner
                .withPropertyValues(
                        "telegram.notifier.enabled=true",
                        "telegram.notifier.token=test-token",
                        "telegram.notifier.username=test-bot",
                        "telegram.notifier.chat-ids=1001,1002"
                )
                .run(context -> assertThat(context).hasBean("telegramNotifyAspect"));
    }

    @Test
    void aspectIsPresentWithNestedRequiredProperties() {
        contextRunner
                .withPropertyValues(
                        "telegram.notifier.enabled=true",
                        "telegram.notifier.bot.token=test-token",
                        "telegram.notifier.bot.username=test-bot",
                        "telegram.notifier.targets.chat-ids=1001,1002",
                        "telegram.notifier.async.enabled=true"
                )
                .run(context -> assertThat(context).hasBean("telegramNotifyAspect"));
    }
}
