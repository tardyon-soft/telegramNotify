package ru.wildred.telegram.notifier.boot2;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.wildred.telegram.notifier.core.config.NotifierConfig;
import ru.wildred.telegram.notifier.core.dispatch.TelegramNotificationDispatcher;
import ru.wildred.telegram.notifier.core.sender.TelegramSender;
import ru.wildred.telegram.notifier.core.template.TemplateEngine;
import ru.wildred.telegram.notifier.spel.DefaultSpelTemplateEngine;
import ru.wildred.telegram.notifier.springaop.TelegramNotifyAspect;
import ru.wildred.telegram.notifier.telegram.DefaultTelegramSender;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TelegramNotifierProperties.class)
@ConditionalOnClass(name = "org.telegram.telegrambots.bots.DefaultAbsSender")
@ConditionalOnProperty(prefix = "telegram.notifier", name = "enabled", havingValue = "true", matchIfMissing = true)
@Conditional(TelegramNotifierEnabledCondition.class)
public class TelegramNotifierAutoConfiguration {

    @Bean(name = "telegramNotifierExecutor")
    @ConditionalOnMissingBean(name = "telegramNotifierExecutor")
    public TaskExecutor telegramNotifierExecutor(TelegramNotifierProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("telegram-notifier-");
        executor.setCorePoolSize(properties.getExecutorCorePoolSize());
        executor.setMaxPoolSize(properties.getExecutorMaxPoolSize());
        executor.setQueueCapacity(properties.getExecutorQueueCapacity());
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean(NotifierConfig.class)
    public NotifierConfig notifierConfig(
            TelegramNotifierProperties properties,
            @Qualifier("telegramNotifierExecutor") TaskExecutor taskExecutor
    ) {
        return new Boot2NotifierConfigAdapter(properties, taskExecutor);
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine telegramTemplateEngine() {
        return new DefaultSpelTemplateEngine();
    }

    @Bean
    @ConditionalOnMissingBean(TelegramSender.class)
    public TelegramSender telegramSender(NotifierConfig notifierConfig) {
        return new DefaultTelegramSender(notifierConfig);
    }

    @Bean
    @ConditionalOnMissingBean(TelegramNotificationDispatcher.class)
    public TelegramNotificationDispatcher telegramNotificationDispatcher(
            NotifierConfig notifierConfig,
            TemplateEngine templateEngine,
            TelegramSender telegramSender,
            @Qualifier("telegramNotifierExecutor") TaskExecutor taskExecutor
    ) {
        Executor executor = taskExecutor;
        return new TelegramNotificationDispatcher(notifierConfig, templateEngine, telegramSender, executor);
    }

    @Bean
    @ConditionalOnMissingBean(TelegramNotifyAspect.class)
    public TelegramNotifyAspect telegramNotifyAspect(TelegramNotificationDispatcher dispatcher) {
        return new TelegramNotifyAspect(dispatcher);
    }
}
