package ru.tardyon.maven.telegram.notifier.core.dispatch;

import java.util.List;

/**
 * Опции для отправки уведомлений в Telegram.
 *
 * <p>Содержит дополнительные параметры, которые могут переопределить
 * глобальные настройки для конкретного уведомления:
 * <ul>
 *   <li>Список идентификаторов чатов для отправки</li>
 *   <li>Режим парсинга сообщения (HTML, Markdown и т.д.)</li>
 *   <li>Политика обработки ошибок при отправке</li>
 * </ul>
 */
public final class NotificationOptions {
  private final List<Long> chatIdsOverride;
  private final ParseMode parseMode;
  private final ErrorPolicy errorPolicy;

  /**
   * Создает новый экземпляр опций уведомления.
   *
   * @param chatIdsOverride список идентификаторов чатов для переопределения глобальных настроек,
   *                        может быть {@code null} для использования настроек по умолчанию
   * @param parseMode       режим парсинга текста сообщения (HTML, Markdown и т.д.),
   *                        может быть {@code null} для использования настроек по умолчанию
   * @param errorPolicy     политика обработки ошибок при отправке уведомлений,
   *                        может быть {@code null} для использования настроек по умолчанию
   */
  public NotificationOptions(
      List<Long> chatIdsOverride,
      ParseMode parseMode,
      ErrorPolicy errorPolicy) {
    this.chatIdsOverride = chatIdsOverride;
    this.parseMode = parseMode;
    this.errorPolicy = errorPolicy;
  }

  /**
   * Возвращает список идентификаторов чатов для переопределения.
   *
   * <p>Если возвращается непустой список, уведомление будет отправлено
   * только в указанные чаты, игнорируя глобальные настройки.
   *
   * @return список идентификаторов чатов или {@code null}, если переопределение не требуется
   */
  public List<Long> chatIdsOverride() {
    return chatIdsOverride;
  }

  /**
   * Возвращает режим парсинга текста сообщения.
   *
   * <p>Определяет, как Telegram будет интерпретировать форматирование
   * в тексте сообщения (например, HTML-теги или Markdown-разметку).
   *
   * @return режим парсинга или {@code null} для использования настроек по умолчанию
   */
  public ParseMode parseMode() {
    return parseMode;
  }

  /**
   * Возвращает политику обработки ошибок при отправке уведомлений.
   *
   * <p>Определяет поведение системы в случае неудачной отправки:
   * повторный выброс исключения, только логирование или игнорирование.
   *
   * @return политика обработки ошибок или {@code null} для использования настроек по умолчанию
   */
  public ErrorPolicy errorPolicy() {
    return errorPolicy;
  }
}
