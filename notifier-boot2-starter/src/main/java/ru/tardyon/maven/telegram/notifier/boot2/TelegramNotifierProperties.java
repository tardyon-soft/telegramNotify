package ru.tardyon.maven.telegram.notifier.boot2;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

/**
 * Класс конфигурационных свойств для Telegram уведомлений.
 *
 * <p>Используется для настройки параметров отправки уведомлений через Telegram Bot API.
 * Свойства читаются из конфигурации приложения с префиксом {@code telegram.notifier}.
 *
 * <p>Поддерживает как прямую конфигурацию через свойства верхнего уровня,
 * так и вложенные объекты конфигурации ({@link BotProperties}, {@link TargetsProperties},
 * {@link AsyncProperties}).
 */
@ConfigurationProperties("telegram.notifier")
public class TelegramNotifierProperties {
  /**
   * Флаг включения/отключения функциональности уведомлений. По умолчанию {@code true}.
   */
  private boolean enabled = true;

  /**
   * Токен Telegram бота для аутентификации в Bot API.
   */
  private String token;

  /**
   * Имя пользователя Telegram бота.
   */
  private String username;

  /**
   * Список идентификаторов чатов для отправки уведомлений.
   */
  private List<Long> chatIds = new ArrayList<>();

  /**
   * Флаг включения асинхронной отправки уведомлений.
   */
  private boolean asyncEnabled;

  /**
   * Вложенные свойства конфигурации бота.
   */
  private BotProperties bot = new BotProperties();

  /**
   * Вложенные свойства конфигурации получателей уведомлений.
   */
  private TargetsProperties targets = new TargetsProperties();

  /**
   * Вложенные свойства конфигурации асинхронной обработки.
   */
  private AsyncProperties async = new AsyncProperties();

  /**
   * Флаг отключения предпросмотра веб-страниц в сообщениях.
   */
  private boolean disableWebPagePreview;

  /**
   * Режим парсинга текста сообщений. По умолчанию {@link ParseMode#PLAIN}.
   */
  private ParseMode parseMode = ParseMode.PLAIN;

  /**
   * Политика обработки ошибок при отправке уведомлений. По умолчанию {@link ErrorPolicy#LOG_ONLY}.
   */
  private ErrorPolicy errorPolicy = ErrorPolicy.LOG_ONLY;

  /**
   * Базовый размер пула потоков для выполнения задач. По умолчанию 1.
   */
  private int executorCorePoolSize = 1;

  /**
   * Максимальный размер пула потоков для выполнения задач. По умолчанию 1.
   */
  private int executorMaxPoolSize = 1;

  /**
   * Емкость очереди задач для executor. По умолчанию 100.
   */
  private int executorQueueCapacity = 100;

  /**
   * Возвращает флаг включения функциональности уведомлений.
   *
   * @return {@code true} если уведомления включены, {@code false} иначе
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Устанавливает флаг включения функциональности уведомлений.
   *
   * @param enabled {@code true} для включения, {@code false} для отключения
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Возвращает токен Telegram бота.
   *
   * <p>Если токен установлен напрямую, возвращается он. Иначе возвращается токен из
   * {@link BotProperties}.
   *
   * @return токен бота или {@code null} если не задан
   */
  public String getToken() {
    if (hasText(token)) {
      return token;
    }
    return bot != null ? bot.getToken() : null;
  }

  /**
   * Устанавливает токен Telegram бота.
   *
   * @param token токен для аутентификации в Bot API
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * Возвращает имя пользователя Telegram бота.
   *
   * <p>Если имя установлено напрямую, возвращается оно. Иначе возвращается имя из
   * {@link BotProperties}.
   *
   * @return имя пользователя бота или {@code null} если не задано
   */
  public String getUsername() {
    if (hasText(username)) {
      return username;
    }
    return bot != null ? bot.getUsername() : null;
  }

  /**
   * Устанавливает имя пользователя Telegram бота.
   *
   * @param username имя пользователя бота
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Возвращает список идентификаторов чатов для отправки уведомлений.
   *
   * <p>Если список установлен напрямую и не пуст, возвращается он.
   * Иначе возвращается список из {@link TargetsProperties}.
   *
   * @return список идентификаторов чатов
   */
  public List<Long> getChatIds() {
    if (chatIds != null && !chatIds.isEmpty()) {
      return chatIds;
    }
    if (targets != null && targets.getChatIds() != null) {
      return targets.getChatIds();
    }
    return chatIds;
  }

  /**
   * Устанавливает список идентификаторов чатов для отправки уведомлений.
   *
   * @param chatIds список идентификаторов чатов
   */
  public void setChatIds(List<Long> chatIds) {
    this.chatIds = chatIds;
  }

  /**
   * Возвращает флаг включения асинхронной отправки уведомлений.
   *
   * <p>Если флаг установлен напрямую в {@code true}, возвращается {@code true}.
   * Иначе возвращается значение из {@link AsyncProperties}.
   *
   * @return {@code true} если асинхронная отправка включена, {@code false} иначе
   */
  public boolean isAsyncEnabled() {
    if (asyncEnabled) {
      return true;
    }
    if (async != null) {
      return async.isEnabled();
    }
    return asyncEnabled;
  }

  /**
   * Устанавливает флаг включения асинхронной отправки уведомлений.
   *
   * @param asyncEnabled {@code true} для включения асинхронной отправки, {@code false} иначе
   */
  public void setAsyncEnabled(boolean asyncEnabled) {
    this.asyncEnabled = asyncEnabled;
  }

  /**
   * Возвращает вложенные свойства конфигурации бота.
   *
   * @return объект {@link BotProperties}
   */
  public BotProperties getBot() {
    return bot;
  }

  /**
   * Устанавливает вложенные свойства конфигурации бота.
   *
   * @param bot объект {@link BotProperties}
   */
  public void setBot(BotProperties bot) {
    this.bot = bot;
  }

  /**
   * Возвращает вложенные свойства конфигурации получателей уведомлений.
   *
   * @return объект {@link TargetsProperties}
   */
  public TargetsProperties getTargets() {
    return targets;
  }

  /**
   * Устанавливает вложенные свойства конфигурации получателей уведомлений.
   *
   * @param targets объект {@link TargetsProperties}
   */
  public void setTargets(TargetsProperties targets) {
    this.targets = targets;
  }

  /**
   * Возвращает вложенные свойства конфигурации асинхронной обработки.
   *
   * @return объект {@link AsyncProperties}
   */
  public AsyncProperties getAsync() {
    return async;
  }

  /**
   * Устанавливает вложенные свойства конфигурации асинхронной обработки.
   *
   * @param async объект {@link AsyncProperties}
   */
  public void setAsync(AsyncProperties async) {
    this.async = async;
  }

  /**
   * Возвращает флаг отключения предпросмотра веб-страниц в сообщениях.
   *
   * @return {@code true} если предпросмотр отключен, {@code false} иначе
   */
  public boolean isDisableWebPagePreview() {
    return disableWebPagePreview;
  }

  /**
   * Устанавливает флаг отключения предпросмотра веб-страниц в сообщениях.
   *
   * @param disableWebPagePreview {@code true} для отключения предпросмотра, {@code false} иначе
   */
  public void setDisableWebPagePreview(boolean disableWebPagePreview) {
    this.disableWebPagePreview = disableWebPagePreview;
  }

  /**
   * Возвращает режим парсинга текста сообщений.
   *
   * @return режим парсинга из перечисления {@link ParseMode}
   */
  public ParseMode getParseMode() {
    return parseMode;
  }

  /**
   * Устанавливает режим парсинга текста сообщений.
   *
   * @param parseMode режим парсинга (PLAIN, MARKDOWN, HTML)
   */
  public void setParseMode(ParseMode parseMode) {
    this.parseMode = parseMode;
  }

  /**
   * Возвращает политику обработки ошибок при отправке уведомлений.
   *
   * @return политика обработки ошибок из перечисления {@link ErrorPolicy}
   */
  public ErrorPolicy getErrorPolicy() {
    return errorPolicy;
  }

  /**
   * Устанавливает политику обработки ошибок при отправке уведомлений.
   *
   * @param errorPolicy политика обработки ошибок (LOG_ONLY, RETHROW и т.д.)
   */
  public void setErrorPolicy(ErrorPolicy errorPolicy) {
    this.errorPolicy = errorPolicy;
  }

  /**
   * Возвращает базовый размер пула потоков для выполнения задач.
   *
   * @return базовый размер пула потоков
   */
  public int getExecutorCorePoolSize() {
    return executorCorePoolSize;
  }

  /**
   * Устанавливает базовый размер пула потоков для выполнения задач.
   *
   * @param executorCorePoolSize базовый размер пула потоков
   */
  public void setExecutorCorePoolSize(int executorCorePoolSize) {
    this.executorCorePoolSize = executorCorePoolSize;
  }

  /**
   * Возвращает максимальный размер пула потоков для выполнения задач.
   *
   * @return максимальный размер пула потоков
   */
  public int getExecutorMaxPoolSize() {
    return executorMaxPoolSize;
  }

  /**
   * Устанавливает максимальный размер пула потоков для выполнения задач.
   *
   * @param executorMaxPoolSize максимальный размер пула потоков
   */
  public void setExecutorMaxPoolSize(int executorMaxPoolSize) {
    this.executorMaxPoolSize = executorMaxPoolSize;
  }

  /**
   * Возвращает емкость очереди задач для executor.
   *
   * @return емкость очереди задач
   */
  public int getExecutorQueueCapacity() {
    return executorQueueCapacity;
  }

  /**
   * Устанавливает емкость очереди задач для executor.
   *
   * @param executorQueueCapacity емкость очереди задач
   */
  public void setExecutorQueueCapacity(int executorQueueCapacity) {
    this.executorQueueCapacity = executorQueueCapacity;
  }

  /**
   * Проверяет, содержит ли строка текст (не {@code null} и не пустая после удаления пробелов).
   *
   * @param value проверяемая строка
   * @return {@code true} если строка содержит текст, {@code false} иначе
   */
  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  /**
   * Вложенные свойства конфигурации Telegram бота.
   *
   * <p>Содержит основные параметры для подключения к Bot API.
   */
  public static class BotProperties {
    /**
     * Токен Telegram бота для аутентификации.
     */
    private String token;

    /**
     * Имя пользователя Telegram бота.
     */
    private String username;

    /**
     * Возвращает токен Telegram бота.
     *
     * @return токен бота
     */
    public String getToken() {
      return token;
    }

    /**
     * Устанавливает токен Telegram бота.
     *
     * @param token токен для аутентификации в Bot API
     */
    public void setToken(String token) {
      this.token = token;
    }

    /**
     * Возвращает имя пользователя Telegram бота.
     *
     * @return имя пользователя бота
     */
    public String getUsername() {
      return username;
    }

    /**
     * Устанавливает имя пользователя Telegram бота.
     *
     * @param username имя пользователя бота
     */
    public void setUsername(String username) {
      this.username = username;
    }
  }

  /**
   * Вложенные свойства конфигурации получателей уведомлений.
   *
   * <p>Содержит список идентификаторов чатов, в которые будут отправляться уведомления.
   */
  public static class TargetsProperties {
    /**
     * Список идентификаторов чатов для отправки уведомлений.
     */
    private List<Long> chatIds = new ArrayList<>();

    /**
     * Возвращает список идентификаторов чатов для отправки уведомлений.
     *
     * @return список идентификаторов чатов
     */
    public List<Long> getChatIds() {
      return chatIds;
    }

    /**
     * Устанавливает список идентификаторов чатов для отправки уведомлений.
     *
     * @param chatIds список идентификаторов чатов
     */
    public void setChatIds(List<Long> chatIds) {
      this.chatIds = chatIds;
    }
  }

  /**
   * Вложенные свойства конфигурации асинхронной обработки уведомлений.
   *
   * <p>Определяет, должны ли уведомления отправляться асинхронно.
   */
  public static class AsyncProperties {
    /**
     * Флаг включения асинхронной отправки уведомлений.
     */
    private boolean enabled;

    /**
     * Возвращает флаг включения асинхронной отправки уведомлений.
     *
     * @return {@code true} если асинхронная отправка включена, {@code false} иначе
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Устанавливает флаг включения асинхронной отправки уведомлений.
     *
     * @param enabled {@code true} для включения асинхронной отправки, {@code false} иначе
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }
}
