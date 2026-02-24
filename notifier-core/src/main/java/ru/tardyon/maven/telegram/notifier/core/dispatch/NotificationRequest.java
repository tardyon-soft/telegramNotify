package ru.tardyon.maven.telegram.notifier.core.dispatch;

import java.util.List;
import ru.tardyon.maven.telegram.notifier.core.aop.MethodInvocationContext;

/**
 * Запрос на отправку уведомления в Telegram.
 *
 * <p>Неизменяемый класс, содержащий всю информацию, необходимую для формирования
 * и отправки уведомления: шаблон сообщения, условие отправки, параметры уведомления
 * и контекст выполнения метода.
 *
 * <p>Используется системой диспетчеризации уведомлений для обработки запросов,
 * созданных на основе аннотаций
 * {@link ru.tardyon.maven.telegram.notifier.core.annotation.TelegramNotify}.
 */
public final class NotificationRequest {
  private final String messageTemplate;
  private final String conditionTemplate;
  private final NotificationOptions options;
  private final MethodInvocationContext context;

  /**
   * Создает новый запрос на отправку уведомления.
   *
   * @param messageTemplate   шаблон сообщения для отправки (обязательный параметр)
   * @param conditionTemplate шаблон условия отправки (может быть null)
   * @param options           дополнительные параметры уведомления (может быть null)
   * @param context           контекст выполнения метода (если null, используется пустой контекст)
   * @throws IllegalArgumentException если messageTemplate равен null
   */
  public NotificationRequest(
      String messageTemplate,
      String conditionTemplate,
      NotificationOptions options,
      MethodInvocationContext context
  ) {
    if (messageTemplate == null) {
      throw new IllegalArgumentException("messageTemplate must not be null");
    }
    this.messageTemplate = messageTemplate;
    this.conditionTemplate = conditionTemplate;
    this.options = options;
    this.context = context == null ? MethodInvocationContext.empty() : context;
  }

  /**
   * Создает полный запрос на отправку уведомления со всеми параметрами.
   *
   * @param messageTemplate   шаблон сообщения для отправки
   * @param conditionTemplate шаблон условия отправки
   * @param chatIdsOverride   список идентификаторов чатов для переопределения настроек по умолчанию
   * @param parseMode         режим форматирования сообщения (MARKDOWN, HTML или NONE)
   * @param errorPolicy       политика обработки ошибок при отправке уведомления
   * @param context           контекст выполнения метода
   * @return новый экземпляр NotificationRequest
   */
  public static NotificationRequest of(
      String messageTemplate,
      String conditionTemplate,
      List<Long> chatIdsOverride,
      ParseMode parseMode,
      ErrorPolicy errorPolicy,
      MethodInvocationContext context
  ) {
    return new NotificationRequest(
        messageTemplate,
        conditionTemplate,
        new NotificationOptions(chatIdsOverride, parseMode, errorPolicy),
        context
    );
  }

  /**
   * Возвращает шаблон сообщения для отправки.
   *
   * <p>Шаблон может содержать SpEL-выражения для динамической подстановки значений.
   *
   * @return шаблон сообщения (никогда не возвращает null)
   */
  public String messageTemplate() {
    return messageTemplate;
  }

  /**
   * Возвращает шаблон условия отправки уведомления.
   *
   * <p>Условие представляет собой SpEL-выражение, результат вычисления которого
   * определяет, будет ли отправлено уведомление.
   *
   * @return шаблон условия или null, если условие не задано
   */
  public String conditionTemplate() {
    return conditionTemplate;
  }

  /**
   * Возвращает дополнительные параметры уведомления.
   *
   * <p>Включает переопределение списка чатов, режим форматирования и политику обработки ошибок.
   *
   * @return параметры уведомления или null, если используются настройки по умолчанию
   */
  public NotificationOptions options() {
    return options;
  }

  /**
   * Возвращает контекст выполнения метода.
   *
   * <p>Контекст содержит информацию о методе, его аргументах, результате выполнения
   * и возможном исключении.
   *
   * @return контекст выполнения метода (никогда не возвращает null)
   */
  public MethodInvocationContext context() {
    return context;
  }
}
