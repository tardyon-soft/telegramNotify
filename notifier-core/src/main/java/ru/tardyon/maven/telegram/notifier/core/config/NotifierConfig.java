package ru.tardyon.maven.telegram.notifier.core.config;

import java.util.List;
import java.util.concurrent.Executor;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

/**
 * Интерфейс конфигурации системы уведомлений Telegram.
 *
 * <p>Определяет основные параметры для отправки уведомлений через Telegram Bot API,
 * включая настройки чатов, токен бота, режимы парсинга и политики обработки ошибок.
 */
public interface NotifierConfig {
  /**
   * Возвращает список идентификаторов чатов по умолчанию для отправки уведомлений.
   *
   * @return список идентификаторов чатов
   */
  List<Long> defaultChatIds();

  /**
   * Возвращает токен Telegram бота для аутентификации в API.
   *
   * @return токен бота, по умолчанию пустая строка
   */
  default String botToken() {
    return "";
  }

  /**
   * Определяет, нужно ли отключать предпросмотр веб-страниц в сообщениях.
   *
   * <p>Если true, ссылки в сообщениях не будут разворачиваться в превью.
   *
   * @return true, если предпросмотр отключен, по умолчанию false
   */
  default boolean disableWebPagePreview() {
    return false;
  }

  /**
   * Определяет, включена ли асинхронная отправка уведомлений.
   *
   * <p>Если true, уведомления будут отправляться в отдельном потоке с использованием
   * исполнителя, возвращаемого методом {@link #asyncExecutor()}.
   *
   * @return true, если асинхронный режим включен, по умолчанию false
   */
  default boolean asyncEnabled() {
    return false;
  }

  /**
   * Возвращает исполнитель для асинхронной отправки уведомлений.
   *
   * <p>Используется только если {@link #asyncEnabled()} возвращает true.
   *
   * @return исполнитель для асинхронных задач, по умолчанию null
   */
  default Executor asyncExecutor() {
    return null;
  }

  /**
   * Возвращает режим парсинга текста сообщений.
   *
   * <p>Определяет, как будет интерпретироваться текст сообщения:
   * обычный текст, Markdown или HTML.
   *
   * @return режим парсинга, по умолчанию {@link ParseMode#PLAIN}
   */
  default ParseMode parseMode() {
    return ParseMode.PLAIN;
  }

  /**
   * Возвращает политику обработки ошибок при отправке уведомлений.
   *
   * <p>Определяет поведение системы в случае возникновения ошибки при отправке уведомления.
   *
   * @return политика обработки ошибок, по умолчанию {@link ErrorPolicy#LOG_ONLY}
   */
  default ErrorPolicy errorPolicy() {
    return ErrorPolicy.LOG_ONLY;
  }
}
