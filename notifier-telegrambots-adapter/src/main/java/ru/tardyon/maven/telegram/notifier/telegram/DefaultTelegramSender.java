package ru.tardyon.maven.telegram.notifier.telegram;

import java.util.List;
import java.util.Objects;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tardyon.maven.telegram.notifier.core.config.NotifierConfig;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;
import ru.tardyon.maven.telegram.notifier.core.sender.TelegramSendException;
import ru.tardyon.maven.telegram.notifier.core.sender.TelegramSender;

/**
 * Стандартная реализация отправителя сообщений в Telegram.
 *
 * <p>Класс предоставляет функциональность отправки текстовых сообщений
 * в Telegram чаты с поддержкой различных режимов форматирования (HTML, Markdown).
 * Использует Telegram Bot API через библиотеку telegrambots.</p>
 */
public class DefaultTelegramSender implements TelegramSender {
  private final NotifierConfig config;
  private final TelegramRequestExecutor requestExecutor;

  /**
   * Создает экземпляр отправителя с конфигурацией по умолчанию.
   *
   * <p>Автоматически создает стандартный {@link DefaultAbsSender} на основе
   * токена бота из конфигурации.</p>
   *
   * @param config конфигурация нотификатора, не может быть {@code null}
   * @throws NullPointerException     если config равен {@code null}
   * @throws IllegalArgumentException если токен бота пустой или отсутствует
   */
  public DefaultTelegramSender(NotifierConfig config) {
    this(config, createDefaultAbsSender(config));
  }

  /**
   * Создает экземпляр отправителя с указанным {@link DefaultAbsSender}.
   *
   * <p>Позволяет использовать кастомный sender для выполнения запросов к Telegram API.</p>
   *
   * @param config конфигурация нотификатора, не может быть {@code null}
   * @param sender отправитель для выполнения запросов, не может быть {@code null}
   * @throws NullPointerException если config или sender равен {@code null}
   */
  public DefaultTelegramSender(NotifierConfig config, DefaultAbsSender sender) {
    this(config, new DefaultAbsSenderRequestExecutor(sender));
  }

  /**
   * Создает экземпляр отправителя с указанным исполнителем запросов.
   *
   * <p>Основной конструктор, позволяющий полностью кастомизировать механизм
   * выполнения запросов к Telegram API.</p>
   *
   * @param config          конфигурация нотификатора, не может быть {@code null}
   * @param requestExecutor исполнитель запросов к Telegram API, не может быть {@code null}
   * @throws NullPointerException если config или requestExecutor равен {@code null}
   */
  public DefaultTelegramSender(NotifierConfig config, TelegramRequestExecutor requestExecutor) {
    this.config = Objects.requireNonNull(config, "config");
    this.requestExecutor = Objects.requireNonNull(requestExecutor, "requestExecutor");
  }

  /**
   * Отправляет текстовое сообщение в указанный чат Telegram.
   *
   * <p>Сообщение форматируется согласно указанному режиму парсинга (HTML, Markdown и т.д.).
   * Настройки предпросмотра веб-страниц берутся из конфигурации.</p>
   *
   * @param chatId    идентификатор чата для отправки сообщения
   * @param message   текст сообщения для отправки
   * @param parseMode режим форматирования текста (PLAIN, HTML, MARKDOWN, MARKDOWN_V2)
   * @throws TelegramSendException если не удалось отправить сообщение
   */
  @Override
  public void send(long chatId, String message, ParseMode parseMode) throws TelegramSendException {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(String.valueOf(chatId));
    sendMessage.setText(message);
    sendMessage.setDisableWebPagePreview(config.disableWebPagePreview());

    String parseModeValue = mapParseMode(parseMode);
    if (parseModeValue != null) {
      sendMessage.setParseMode(parseModeValue);
    }

    try {
      requestExecutor.execute(sendMessage);
    } catch (TelegramApiException ex) {
      throw new TelegramSendException("Failed to send telegram message", ex);
    }
  }

  /**
   * Отправляет одно и то же сообщение в несколько чатов Telegram.
   *
   * <p>Метод последовательно отправляет сообщение в каждый чат из списка.
   * Пропускает {@code null} значения идентификаторов чатов.
   * Если список пустой или {@code null}, метод ничего не делает.</p>
   *
   * @param chatIds   список идентификаторов чатов для рассылки
   * @param message   текст сообщения для отправки
   * @param parseMode режим форматирования текста
   * @throws TelegramSendException если не удалось отправить сообщение хотя бы в один чат
   */
  public void sendMany(List<Long> chatIds, String message, ParseMode parseMode)
      throws TelegramSendException {
    if (chatIds == null || chatIds.isEmpty()) {
      return;
    }
    for (Long chatId : chatIds) {
      if (chatId != null) {
        send(chatId, message, parseMode);
      }
    }
  }

  private static DefaultAbsSender createDefaultAbsSender(NotifierConfig config) {
    Objects.requireNonNull(config, "config");
    final String token = config.botToken();
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("botToken must not be blank");
    }
    return new DefaultAbsSender(new DefaultBotOptions()) {
      @Override
      public String getBotToken() {
        return token;
      }
    };
  }

  private String mapParseMode(ParseMode parseMode) {
    if (parseMode == null || parseMode == ParseMode.PLAIN) {
      return null;
    }
    if (parseMode == ParseMode.HTML) {
      return org.telegram.telegrambots.meta.api.methods.ParseMode.HTML;
    }
    if (parseMode == ParseMode.MARKDOWN_V2) {
      return org.telegram.telegrambots.meta.api.methods.ParseMode.MARKDOWNV2;
    }
    return org.telegram.telegrambots.meta.api.methods.ParseMode.MARKDOWN;
  }

  /**
   * Интерфейс для выполнения запросов отправки сообщений в Telegram API.
   *
   * <p>Позволяет абстрагировать механизм выполнения запросов и облегчает тестирование.</p>
   */
  public interface TelegramRequestExecutor {
    /**
     * Выполняет запрос на отправку сообщения в Telegram.
     *
     * @param sendMessage объект с параметрами отправляемого сообщения
     * @throws TelegramApiException если произошла ошибка при выполнении запроса к API
     */
    void execute(SendMessage sendMessage) throws TelegramApiException;
  }

  private static final class DefaultAbsSenderRequestExecutor implements TelegramRequestExecutor {
    private final DefaultAbsSender sender;

    private DefaultAbsSenderRequestExecutor(DefaultAbsSender sender) {
      this.sender = Objects.requireNonNull(sender, "sender");
    }

    @Override
    public void execute(SendMessage sendMessage) throws TelegramApiException {
      sender.execute(sendMessage);
    }
  }
}
