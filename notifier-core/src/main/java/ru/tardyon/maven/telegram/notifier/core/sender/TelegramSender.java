package ru.tardyon.maven.telegram.notifier.core.sender;

import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

/**
 * Интерфейс для отправки сообщений в Telegram.
 *
 * <p>Определяет контракт для реализаций, отвечающих за отправку
 * текстовых сообщений в чаты Telegram с поддержкой различных
 * режимов форматирования.
 */
public interface TelegramSender {
  /**
   * Отправляет текстовое сообщение в указанный чат Telegram.
   *
   * @param chatId    идентификатор чата, в который отправляется сообщение
   * @param message   текст сообщения для отправки
   * @param parseMode режим форматирования сообщения (HTML, Markdown и т.д.)
   * @throws TelegramSendException если произошла ошибка при отправке сообщения
   */
  void send(long chatId, String message, ParseMode parseMode) throws TelegramSendException;
}
