package ru.tardyon.maven.telegram.notifier.boot2;

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
import ru.tardyon.maven.telegram.notifier.core.config.NotifierConfig;
import ru.tardyon.maven.telegram.notifier.core.dispatch.TelegramNotificationDispatcher;
import ru.tardyon.maven.telegram.notifier.core.sender.TelegramSender;
import ru.tardyon.maven.telegram.notifier.core.template.TemplateEngine;
import ru.tardyon.maven.telegram.notifier.spel.DefaultSpelTemplateEngine;
import ru.tardyon.maven.telegram.notifier.springaop.TelegramNotifyAspect;
import ru.tardyon.maven.telegram.notifier.telegram.DefaultTelegramSender;

/**
 * Автоконфигурация для Telegram Notifier в Spring Boot 2.x приложениях.
 *
 * <p>Автоматически создает и настраивает необходимые компоненты для отправки
 * уведомлений в Telegram через аннотацию {@code @TelegramNotify}.
 *
 * <p>Условия активации автоконфигурации:
 * <ul>
 *   <li>В classpath присутствует класс {@code org.telegram.telegrambots.bots.DefaultAbsSender}</li>
 *   <li>Свойство {@code telegram.notifier.enabled} не установлено в
 *   {@code false} (по умолчанию {@code true})</li>
 *   <li>Выполняется дополнительное условие {@link TelegramNotifierEnabledCondition}</li>
 * </ul>
 *
 * <p>Создаваемые bean-компоненты:
 * <ul>
 *   <li>{@link TaskExecutor} - пул потоков для асинхронной отправки уведомлений</li>
 *   <li>{@link NotifierConfig} - конфигурация на основе Spring Boot properties</li>
 *   <li>{@link TemplateEngine} - движок для обработки SpEL-выражений в сообщениях</li>
 *   <li>{@link TelegramSender} - компонент для отправки сообщений в Telegram API</li>
 *   <li>{@link TelegramNotificationDispatcher} - диспетчер для управления процессом отправки</li>
 *   <li>{@link TelegramNotifyAspect} - аспект для перехвата методов с аннотацией
 *   {@code @TelegramNotify}</li>
 * </ul>
 *
 * @see TelegramNotifierProperties
 * @see TelegramNotifierEnabledCondition
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TelegramNotifierProperties.class)
@ConditionalOnClass(name = "org.telegram.telegrambots.bots.DefaultAbsSender")
@ConditionalOnProperty(prefix = "telegram.notifier", name = "enabled",
    havingValue = "true", matchIfMissing = true)
@Conditional(TelegramNotifierEnabledCondition.class)
public class TelegramNotifierAutoConfiguration {

  /**
   * Создает пул потоков для асинхронной отправки Telegram-уведомлений.
   *
   * <p>Конфигурируется через свойства {@link TelegramNotifierProperties}:
   * <ul>
   *   <li>{@code telegram.notifier.executor-core-pool-size} - базовый размер пула</li>
   *   <li>{@code telegram.notifier.executor-max-pool-size} - максимальный размер пула</li>
   *   <li>{@code telegram.notifier.executor-queue-capacity} - размер очереди задач</li>
   * </ul>
   *
   * <p>Bean создается только если не существует другого bean с именем
   * {@code telegramNotifierExecutor}.
   *
   * @param properties конфигурационные свойства для настройки пула потоков
   * @return настроенный {@link TaskExecutor} для отправки уведомлений
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
   * Создает конфигурацию уведомителя на основе Spring Boot properties.
   *
   * <p>Адаптирует {@link TelegramNotifierProperties} к интерфейсу {@link NotifierConfig},
   * используя {@link Boot2NotifierConfigAdapter} для совместимости со Spring Boot 2.x.
   *
   * <p>Bean создается только если не существует другого bean типа {@link NotifierConfig}.
   *
   * @param properties   конфигурационные свойства Telegram Notifier
   * @param taskExecutor пул потоков для асинхронных операций
   * @return адаптер конфигурации для Spring Boot 2.x
   */
  @Bean
  @ConditionalOnMissingBean(NotifierConfig.class)
  public NotifierConfig notifierConfig(
      TelegramNotifierProperties properties,
      @Qualifier("telegramNotifierExecutor") TaskExecutor taskExecutor
  ) {
    return new Boot2NotifierConfigAdapter(properties, taskExecutor);
  }

  /**
   * Создает движок шаблонов для обработки SpEL-выражений в сообщениях уведомлений.
   *
   * <p>Используется реализация {@link DefaultSpelTemplateEngine}, которая позволяет
   * использовать Spring Expression Language (SpEL) для динамического формирования
   * текста уведомлений на основе параметров метода, результата выполнения и исключений.
   *
   * <p>Bean создается только если не существует другого bean типа {@link TemplateEngine}.
   *
   * @return движок шаблонов на основе SpEL
   */
  @Bean
  @ConditionalOnMissingBean(TemplateEngine.class)
  public TemplateEngine telegramTemplateEngine() {
    return new DefaultSpelTemplateEngine();
  }

  /**
   * Создает компонент для отправки сообщений в Telegram API.
   *
   * <p>Использует реализацию {@link DefaultTelegramSender}, которая работает с
   * Telegram Bot API через библиотеку {@code telegram-bots}.
   *
   * <p>Bean создается только если не существует другого bean типа {@link TelegramSender}.
   *
   * @param notifierConfig конфигурация с параметрами подключения к Telegram
   * @return отправитель сообщений в Telegram
   */
  @Bean
  @ConditionalOnMissingBean(TelegramSender.class)
  public TelegramSender telegramSender(NotifierConfig notifierConfig) {
    return new DefaultTelegramSender(notifierConfig);
  }

  /**
   * Создает диспетчер для управления процессом отправки уведомлений.
   *
   * <p>{@link TelegramNotificationDispatcher} координирует работу всех компонентов:
   * <ul>
   *   <li>Обрабатывает шаблоны сообщений через {@link TemplateEngine}</li>
   *   <li>Применяет условия отправки (condition)</li>
   *   <li>Отправляет сообщения через {@link TelegramSender}</li>
   *   <li>Выполняет отправку асинхронно через {@link TaskExecutor}</li>
   *   <li>Обрабатывает ошибки согласно политике {@code ErrorPolicy}</li>
   * </ul>
   *
   * <p>Bean создается только если не существует другого bean типа
   * {@link TelegramNotificationDispatcher}.
   *
   * @param notifierConfig конфигурация уведомителя
   * @param templateEngine движок обработки шаблонов сообщений
   * @param telegramSender отправитель сообщений в Telegram
   * @param taskExecutor   пул потоков для асинхронной отправки
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
    return new TelegramNotificationDispatcher(notifierConfig,
        templateEngine, telegramSender, taskExecutor);
  }

  /**
   * Создает аспект для перехвата вызовов методов с аннотацией {@code @TelegramNotify}.
   *
   * <p>{@link TelegramNotifyAspect} использует Spring AOP для:
   * <ul>
   *   <li>Перехвата методов, помеченных {@code @TelegramNotify}</li>
   *   <li>Извлечения параметров метода, результата и исключений</li>
   *   <li>Передачи информации в {@link TelegramNotificationDispatcher} для отправки</li>
   *   <li>Соблюдения режима отправки (
   *   {@code NotifyWhen.BEFORE/AFTER_SUCCESS/AFTER_FAILURE/AFTER_FINALLY})</li>
   * </ul>
   *
   * <p>Bean создается только если не существует другого bean типа {@link TelegramNotifyAspect}.
   *
   * @param dispatcher диспетчер для отправки уведомлений
   * @return аспект для обработки аннотаций {@code @TelegramNotify}
   */
  @Bean
  @ConditionalOnMissingBean(TelegramNotifyAspect.class)
  public TelegramNotifyAspect telegramNotifyAspect(TelegramNotificationDispatcher dispatcher) {
    return new TelegramNotifyAspect(dispatcher);
  }
}
