package ru.tardyon.maven.telegram.notifier.boot2;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import ru.tardyon.maven.telegram.notifier.core.config.NotifierConfig;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

/**
 * Адаптер конфигурации для Spring Boot 2, преобразующий {@link TelegramNotifierProperties}
 * в интерфейс {@link NotifierConfig}.
 *
 * <p>Предоставляет мост между настройками Spring Boot и внутренней конфигурацией нотификатора.
 */
public class Boot2NotifierConfigAdapter implements NotifierConfig {
  private final TelegramNotifierProperties properties;
  private final Executor asyncExecutor;

  /**
   * Создает новый адаптер конфигурации.
   *
   * @param properties    настройки Telegram нотификатора из Spring Boot
   * @param asyncExecutor исполнитель для асинхронных операций
   */
  public Boot2NotifierConfigAdapter(
      TelegramNotifierProperties properties,
      Executor asyncExecutor) {
    this.properties = properties;
    this.asyncExecutor = asyncExecutor;
  }

  /**
   * Возвращает список идентификаторов чатов по умолчанию для отправки уведомлений.
   *
   * @return список идентификаторов чатов или пустой список, если они не заданы
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
   * @return токен бота
   */
  @Override
  public String botToken() {
    return properties.getToken();
  }

  /**
   * Возвращает флаг отключения предпросмотра веб-страниц в сообщениях.
   *
   * @return {@code true}, если предпросмотр отключен, иначе {@code false}
   */
  @Override
  public boolean disableWebPagePreview() {
    return properties.isDisableWebPagePreview();
  }

  /**
   * Возвращает флаг включения асинхронного режима отправки уведомлений.
   *
   * @return {@code true}, если асинхронный режим включен, иначе {@code false}
   */
  @Override
  public boolean asyncEnabled() {
    return properties.isAsyncEnabled();
  }

  /**
   * Возвращает исполнитель для асинхронных операций отправки уведомлений.
   *
   * @return исполнитель задач
   */
  @Override
  public Executor asyncExecutor() {
    return asyncExecutor;
  }

  /**
   * Возвращает режим парсинга сообщений (Markdown, HTML и т.д.).
   *
   * @return режим парсинга
   */
  @Override
  public ParseMode parseMode() {
    return properties.getParseMode();
  }

  /**
   * Возвращает политику обработки ошибок при отправке уведомлений.
   *
   * @return политика обработки ошибок
   */
  @Override
  public ErrorPolicy errorPolicy() {
    return properties.getErrorPolicy();
  }
}
