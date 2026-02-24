package ru.tardyon.maven.telegram.notifier.core.sender;

/**
 * Исключение, возникающее при ошибках отправки сообщений в Telegram.
 *
 * <p>Это runtime-исключение используется для индикации проблем,
 * возникающих в процессе взаимодействия с Telegram Bot API.</p>
 */
public class TelegramSendException extends RuntimeException {
  /**
   * Создает новое исключение с указанным сообщением об ошибке.
   *
   * @param message описание ошибки
   */
  public TelegramSendException(String message) {
    super(message);
  }

  /**
   * Создает новое исключение с указанным сообщением об ошибке и причиной.
   *
   * @param message описание ошибки
   * @param cause   исходная причина ошибки
   */
  public TelegramSendException(String message, Throwable cause) {
    super(message, cause);
  }
}
