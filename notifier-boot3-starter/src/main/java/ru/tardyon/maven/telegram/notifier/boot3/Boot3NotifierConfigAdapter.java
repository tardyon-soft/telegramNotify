package ru.tardyon.maven.telegram.notifier.boot3;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import ru.tardyon.maven.telegram.notifier.core.config.NotifierConfig;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

/**
 * Адаптер конфигурации уведомлений для Spring Boot 3.
 *
 * <p>Преобразует свойства {@link TelegramNotifierProperties} в интерфейс {@link NotifierConfig},
 * используемый ядром библиотеки для отправки уведомлений в Telegram.</p>
 */
public class Boot3NotifierConfigAdapter implements NotifierConfig {
  private final TelegramNotifierProperties properties;
  private final Executor asyncExecutor;

  /**
   * Создает адаптер конфигурации.
   *
   * @param properties    свойства уведомителя из Spring Boot конфигурации
   * @param asyncExecutor исполнитель для асинхронной отправки уведомлений
   */
  public Boot3NotifierConfigAdapter(
      TelegramNotifierProperties properties,
      Executor asyncExecutor) {
    this.properties = properties;
    this.asyncExecutor = asyncExecutor;
  }

  /**
   * Возвращает список идентификаторов чатов по умолчанию.
   *
   * @return список ID чатов для отправки уведомлений или пустой список, если не настроены
   */
  @Override
  public List<Long> defaultChatIds() {
    List<Long> chatIds = properties.getChatIds();
    if (chatIds == null) {
      return Collections.emptyList();
    }
    return chatIds;
  }

  /**
   * Возвращает токен Telegram бота.
   *
   * @return токен бота для аутентификации в Telegram API
   */
  @Override
  public String botToken() {
    return properties.getToken();
  }

  /**
   * Проверяет, отключен ли предпросмотр веб-страниц в сообщениях.
   *
   * @return {@code true}, если предпросмотр ссылок отключен, иначе {@code false}
   */
  @Override
  public boolean disableWebPagePreview() {
    return properties.isDisableWebPagePreview();
  }

  /**
   * Проверяет, включена ли асинхронная отправка уведомлений.
   *
   * @return {@code true}, если уведомления отправляются асинхронно, иначе {@code false}
   */
  @Override
  public boolean asyncEnabled() {
    return properties.isAsyncEnabled();
  }

  /**
   * Возвращает исполнитель для асинхронной отправки уведомлений.
   *
   * @return экземпляр {@link Executor} для выполнения асинхронных задач
   */
  @Override
  public Executor asyncExecutor() {
    return asyncExecutor;
  }

  /**
   * Возвращает режим парсинга текста сообщений.
   *
   * @return режим форматирования (Markdown, HTML и т.д.)
   */
  @Override
  public ParseMode parseMode() {
    return properties.getParseMode();
  }

  /**
   * Возвращает политику обработки ошибок при отправке уведомлений.
   *
   * @return стратегия обработки ошибок (логирование, пробрасывание исключений и т.д.)
   */
  @Override
  public ErrorPolicy errorPolicy() {
    return properties.getErrorPolicy();
  }
}
