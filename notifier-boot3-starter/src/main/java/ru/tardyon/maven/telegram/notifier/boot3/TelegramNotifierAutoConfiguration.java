package ru.tardyon.maven.telegram.notifier.boot3;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.tardyon.maven.telegram.notifier.core.config.NotifierConfig;
import ru.tardyon.maven.telegram.notifier.core.dispatch.TelegramNotificationDispatcher;
import ru.tardyon.maven.telegram.notifier.core.sender.TelegramSender;
import ru.tardyon.maven.telegram.notifier.core.template.TemplateEngine;
import ru.tardyon.maven.telegram.notifier.spel.DefaultSpelTemplateEngine;
import ru.tardyon.maven.telegram.notifier.springaop.TelegramNotifyAspect;
import ru.tardyon.maven.telegram.notifier.telegram.DefaultTelegramSender;

/**
 * Автоконфигурация Spring Boot для компонентов Telegram-уведомлений.
 *
 * <p>Автоматически регистрирует необходимые бины для работы системы уведомлений через Telegram:
 * <ul>
 *   <li>Executor для асинхронной отправки уведомлений</li>
 *   <li>Конфигурация нотификатора</li>
 *   <li>Движок шаблонизации сообщений</li>
 *   <li>Отправщик сообщений в Telegram</li>
 *   <li>Диспетчер уведомлений</li>
 *   <li>Аспект для обработки аннотаций
 *   {@link ru.tardyon.maven.telegram.notifier.core.annotation.TelegramNotify}</li>
 * </ul>
 *
 * <p>Условия активации:
 * <ul>
 *   <li>В classpath присутствует класс {@code org.telegram.telegrambots.bots.DefaultAbsSender}</li>
 *   <li>Свойство {@code telegram.notifier.enabled} не установлено в {@code false}</li>
 *   <li>Выполняются условия {@link TelegramNotifierEnabledCondition}</li>
 * </ul>
 *
 * @see TelegramNotifierProperties
 * @see TelegramNotifierEnabledCondition
 */
@AutoConfiguration
@EnableConfigurationProperties(TelegramNotifierProperties.class)
@ConditionalOnClass(name = "org.telegram.telegrambots.bots.DefaultAbsSender")
@ConditionalOnProperty(prefix = "telegram.notifier",
    name = "enabled", havingValue = "true", matchIfMissing = true)
@Conditional(TelegramNotifierEnabledCondition.class)
public class TelegramNotifierAutoConfiguration {

  /**
   * Создаёт executor для асинхронной отправки Telegram-уведомлений.
   *
   * <p>Настраивает пул потоков с параметрами из конфигурации:
   * <ul>
   *   <li>Префикс имени потока: {@code telegram-notifier-}</li>
   *   <li>Размер основного пула:
   *   {@link TelegramNotifierProperties#getExecutorCorePoolSize()}</li>
   *   <li>Максимальный размер пула:
   *   {@link TelegramNotifierProperties#getExecutorMaxPoolSize()}</li>
   *   <li>Размер очереди:
   *   {@link TelegramNotifierProperties#getExecutorQueueCapacity()}</li>
   * </ul>
   *
   * <p>Бин создаётся только если не существует другого бина с именем
   * {@code telegramNotifierExecutor}.
   *
   * @param properties конфигурационные свойства нотификатора
   * @return настроенный executor для отправки уведомлений
   */
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

  /**
   * Создаёт конфигурацию нотификатора на основе Spring Boot 3 properties.
   *
   * <p>Адаптирует {@link TelegramNotifierProperties} к интерфейсу {@link NotifierConfig},
   * используемому core-модулем библиотеки.
   *
   * <p>Бин создаётся только если не существует другого бина типа {@link NotifierConfig}.
   *
   * @param properties   конфигурационные свойства нотификатора
   * @param taskExecutor executor для асинхронной отправки уведомлений
   * @return конфигурация нотификатора
   */
  @Bean
  @ConditionalOnMissingBean(NotifierConfig.class)
  public NotifierConfig notifierConfig(
      TelegramNotifierProperties properties,
      @Qualifier("telegramNotifierExecutor") TaskExecutor taskExecutor
  ) {
    return new Boot3NotifierConfigAdapter(properties, taskExecutor);
  }

  /**
   * Создаёт движок шаблонизации сообщений на основе Spring Expression Language (SpEL).
   *
   * <p>Используется для обработки выражений в сообщениях аннотации
   * {@link ru.tardyon.maven.telegram.notifier.core.annotation.TelegramNotify}.
   *
   * <p>Бин создаётся только если не существует другого бина типа {@link TemplateEngine}.
   *
   * @return движок шаблонизации на основе SpEL
   */
  @Bean
  @ConditionalOnMissingBean(TemplateEngine.class)
  public TemplateEngine telegramTemplateEngine() {
    return new DefaultSpelTemplateEngine();
  }

  /**
   * Создаёт компонент для отправки сообщений в Telegram.
   *
   * <p>Реализует низкоуровневое взаимодействие с Telegram Bot API
   * для доставки уведомлений в указанные чаты.
   *
   * <p>Бин создаётся только если не существует другого бина типа {@link TelegramSender}.
   *
   * @param notifierConfig конфигурация нотификатора с токеном бота и ID чата
   * @return компонент для отправки сообщений в Telegram
   */
  @Bean
  @ConditionalOnMissingBean(TelegramSender.class)
  public TelegramSender telegramSender(NotifierConfig notifierConfig) {
    return new DefaultTelegramSender(notifierConfig);
  }

  /**
   * Создаёт диспетчер для управления процессом отправки уведомлений.
   *
   * <p>Координирует работу шаблонизатора, отправщика и executor'а:
   * <ul>
   *   <li>Обрабатывает шаблоны сообщений</li>
   *   <li>Применяет условия отправки</li>
   *   <li>Управляет асинхронной доставкой</li>
   *   <li>Обрабатывает ошибки согласно политике</li>
   * </ul>
   *
   * <p>Бин создаётся только если не существует другого бина типа
   * {@link TelegramNotificationDispatcher}.
   *
   * @param notifierConfig конфигурация нотификатора
   * @param templateEngine движок шаблонизации сообщений
   * @param telegramSender компонент для отправки сообщений
   * @param taskExecutor   executor для асинхронной отправки
   * @return диспетчер уведомлений
   */
  @Bean
  @ConditionalOnMissingBean(TelegramNotificationDispatcher.class)
  public TelegramNotificationDispatcher telegramNotificationDispatcher(
      NotifierConfig notifierConfig,
      TemplateEngine templateEngine,
      TelegramSender telegramSender,
      @Qualifier("telegramNotifierExecutor") TaskExecutor taskExecutor
  ) {
    return new TelegramNotificationDispatcher(notifierConfig, templateEngine,
        telegramSender, taskExecutor);
  }

  /**
   * Создаёт Spring AOP аспект для обработки аннотации
   * {@link ru.tardyon.maven.telegram.notifier.core.annotation.TelegramNotify}.
   *
   * <p>Перехватывает вызовы аннотированных методов и инициирует отправку
   * уведомлений в зависимости от режима
   * {@link ru.tardyon.maven.telegram.notifier.core.annotation.NotifyWhen}):
   * <ul>
   *   <li>{@code BEFORE} - до выполнения метода</li>
   *   <li>{@code AFTER_SUCCESS} - после успешного выполнения</li>
   *   <li>{@code AFTER_FAILURE} - при возникновении исключения</li>
   *   <li>{@code AFTER_FINALLY} - в любом случае после выполнения</li>
   * </ul>
   *
   * <p>Бин создаётся только если не существует другого бина типа {@link TelegramNotifyAspect}.
   *
   * @param dispatcher диспетчер для отправки уведомлений
   * @return аспект для обработки аннотаций
   */
  @Bean
  @ConditionalOnMissingBean(TelegramNotifyAspect.class)
  public TelegramNotifyAspect telegramNotifyAspect(TelegramNotificationDispatcher dispatcher) {
    return new TelegramNotifyAspect(dispatcher);
  }
}
