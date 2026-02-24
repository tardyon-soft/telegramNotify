package ru.tardyon.maven.telegram.notifier.core.dispatch;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.tardyon.maven.telegram.notifier.core.aop.MethodInvocationContext;
import ru.tardyon.maven.telegram.notifier.core.config.NotifierConfig;
import ru.tardyon.maven.telegram.notifier.core.sender.TelegramSender;
import ru.tardyon.maven.telegram.notifier.core.template.TemplateEngine;

/**
 * Диспетчер уведомлений Telegram, отвечающий за обработку и отправку уведомлений.
 *
 * <p>Класс выполняет следующие функции:
 * <ul>
 *   <li>Оценка условий отправки уведомлений</li>
 *   <li>Рендеринг шаблонов сообщений с использованием контекста вызова метода</li>
 *   <li>Разрешение списка chat ID получателей</li>
 *   <li>Определение режима парсинга (HTML, Markdown и т.д.)</li>
 *   <li>Управление политикой обработки ошибок</li>
 *   <li>Поддержка синхронной и асинхронной отправки</li>
 * </ul>
 *
 * <p>Диспетчер может работать как синхронно, так и асинхронно в зависимости от конфигурации.
 * При асинхронной отправке используется предоставленный {@link Executor}.
 */

public class TelegramNotificationDispatcher {
  private static final Logger LOGGER =
      Logger.getLogger(TelegramNotificationDispatcher.class.getName());

  private final NotifierConfig config;
  private final TemplateEngine templateEngine;
  private final TelegramSender telegramSender;
  private final Executor executor;

  /**
   * Создает новый экземпляр диспетчера уведомлений Telegram.
   *
   * @param config         конфигурация уведомлений,
   *                       содержащая настройки по умолчанию (не может быть null)
   * @param templateEngine движок шаблонов для рендеринга сообщений (не может быть null)
   * @param telegramSender отправитель сообщений в Telegram (не может быть null)
   * @param executor       исполнитель для асинхронной отправки (может быть null,
   *                       тогда используется из config)
   * @throws NullPointerException если config, templateEngine или telegramSender равны null
   */
  public TelegramNotificationDispatcher(
      NotifierConfig config,
      TemplateEngine templateEngine,
      TelegramSender telegramSender,
      Executor executor
  ) {
    this.config = Objects.requireNonNull(config, "config");
    this.templateEngine = Objects.requireNonNull(templateEngine, "templateEngine");
    this.telegramSender = Objects.requireNonNull(telegramSender, "telegramSender");
    this.executor = executor;
  }

  /**
   * Обрабатывает и отправляет уведомление согласно запросу.
   *
   * <p>Процесс обработки включает следующие шаги:
   * <ol>
   *   <li>Проверка условия отправки (condition)</li>
   *   <li>Рендеринг шаблона сообщения</li>
   *   <li>Разрешение списка chat ID получателей</li>
   *   <li>Определение режима парсинга и политики обработки ошибок</li>
   *   <li>Отправка сообщения (синхронно или асинхронно)</li>
   * </ol>
   *
   * <p>Если условие не выполнено, сообщение пустое или список получателей пуст,
   * уведомление не отправляется.
   *
   * @param request запрос на отправку уведомления (не может быть null)
   * @throws NullPointerException если request равен null
   */
  public void dispatch(NotificationRequest request) {
    Objects.requireNonNull(request, "request");

    MethodInvocationContext context = request.context();

    if (!evaluateCondition(request.conditionTemplate(), context)) {
      return;
    }

    String message = templateEngine.render(request.messageTemplate(), context);
    if (message == null || message.isBlank()) {
      return;
    }

    List<Long> chatIds = resolveChatIds(request.options());
    if (chatIds.isEmpty()) {
      return;
    }

    ParseMode parseMode = resolveParseMode(request.options());
    ErrorPolicy errorPolicy = resolveErrorPolicy(request.options());

    Runnable sendTask = () -> {
      for (Long chatId : chatIds) {
        sendSingle(chatId, message, parseMode, errorPolicy);
      }
    };

    Executor effectiveExecutor = executor != null ? executor : config.asyncExecutor();
    if (config.asyncEnabled() && effectiveExecutor != null) {
      effectiveExecutor.execute(sendTask);
      return;
    }

    sendTask.run();
  }

  /**
   * Оценивает условие отправки уведомления.
   *
   * <p>Условие задается в виде шаблона, который рендерится с использованием контекста вызова.
   * Результат рендеринга преобразуется в boolean значение.
   *
   * @param conditionTemplate шаблон условия (может быть null или пустым, тогда возвращается true)
   * @param context           контекст вызова метода для рендеринга шаблона
   * @return true если условие выполнено или не задано, false в противном случае
   */
  private boolean evaluateCondition(String conditionTemplate, MethodInvocationContext context) {
    if (conditionTemplate == null || conditionTemplate.isBlank()) {
      return true;
    }
    String rendered = templateEngine.render(conditionTemplate, context);
    if (rendered == null) {
      return false;
    }
    return Boolean.parseBoolean(rendered.trim().toLowerCase(Locale.ROOT));
  }

  /**
   * Определяет список chat ID получателей уведомления.
   *
   * <p>Приоритет разрешения:
   * <ol>
   *   <li>Chat ID из опций уведомления (если заданы)</li>
   *   <li>Chat ID по умолчанию из конфигурации</li>
   *   <li>Пустой список, если ничего не задано</li>
   * </ol>
   *
   * @param options опции уведомления (может быть null)
   * @return неизменяемый список chat ID получателей (никогда не null, но может быть пустым)
   */
  private List<Long> resolveChatIds(NotificationOptions options) {
    if (options != null && options.chatIdsOverride() != null
        && !options.chatIdsOverride().isEmpty()) {
      return List.copyOf(options.chatIdsOverride());
    }

    List<Long> defaults = config.defaultChatIds();
    if (defaults == null || defaults.isEmpty()) {
      return Collections.emptyList();
    }
    return List.copyOf(defaults);
  }

  /**
   * Определяет режим парсинга сообщения.
   *
   * <p>Приоритет разрешения:
   * <ol>
   *   <li>Режим из опций уведомления (если задан)</li>
   *   <li>Режим по умолчанию из конфигурации</li>
   * </ol>
   *
   * @param options опции уведомления (может быть null)
   * @return режим парсинга сообщения (HTML, Markdown и т.д.)
   */
  private ParseMode resolveParseMode(NotificationOptions options) {
    if (options != null && options.parseMode() != null) {
      return options.parseMode();
    }
    return config.parseMode();
  }

  /**
   * Определяет политику обработки ошибок при отправке.
   *
   * <p>Приоритет разрешения:
   * <ol>
   *   <li>Политика из опций уведомления (если задана)</li>
   *   <li>Политика по умолчанию из конфигурации</li>
   * </ol>
   *
   * @param options опции уведомления (может быть null)
   * @return политика обработки ошибок (THROW или LOG_ONLY)
   */
  private ErrorPolicy resolveErrorPolicy(NotificationOptions options) {
    if (options != null && options.errorPolicy() != null) {
      return options.errorPolicy();
    }
    return config.errorPolicy();
  }

  /**
   * Отправляет уведомление одному получателю.
   *
   * <p>При возникновении ошибки поведение зависит от политики обработки ошибок:
   * <ul>
   *   <li>{@link ErrorPolicy#THROW} - исключение пробрасывается выше</li>
   *   <li>{@link ErrorPolicy#LOG_ONLY} - исключение логируется с уровнем WARNING</li>
   * </ul>
   *
   * @param chatId      идентификатор чата получателя
   * @param message     текст сообщения
   * @param parseMode   режим парсинга сообщения
   * @param errorPolicy политика обработки ошибок
   * @throws RuntimeException если отправка не удалась и errorPolicy == THROW
   */
  private void sendSingle(Long chatId, String message,
                          ParseMode parseMode, ErrorPolicy errorPolicy) {
    try {
      telegramSender.send(chatId, message, parseMode);
    } catch (RuntimeException ex) {
      if (errorPolicy == ErrorPolicy.THROW) {
        throw ex;
      }
      LOGGER.log(Level.WARNING,
          "Failed to send telegram notification to chatId=" + chatId, ex);
    }
  }
}
