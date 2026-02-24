package ru.tardyon.maven.telegram.notifier.boot3;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

/**
 * Свойства конфигурации для Telegram-уведомителя.
 *
 * <p>Класс содержит все настройки, необходимые для работы Telegram-бота,
 * отправляющего уведомления. Поддерживает различные режимы работы,
 * форматирование сообщений и политики обработки ошибок.
 *
 * <p>Свойства читаются из конфигурации Spring с префиксом {@code telegram.notifier}.
 */
@ConfigurationProperties("telegram.notifier")
public class TelegramNotifierProperties {
  /**
   * Флаг включения/выключения уведомителя. По умолчанию {@code true}.
   */
  private boolean enabled = true;

  /**
   * Токен Telegram-бота для аутентификации в API.
   */
  private String token;

  /**
   * Имя пользователя (username) Telegram-бота.
   */
  private String username;

  /**
   * Список идентификаторов чатов, в которые будут отправляться уведомления.
   */
  private List<Long> chatIds = new ArrayList<>();

  /**
   * Флаг включения асинхронного режима отправки уведомлений.
   */
  private boolean asyncEnabled;

  /**
   * Вложенные свойства конфигурации бота.
   */
  private BotProperties bot = new BotProperties();

  /**
   * Вложенные свойства конфигурации целевых чатов.
   */
  private TargetsProperties targets = new TargetsProperties();

  /**
   * Вложенные свойства конфигурации асинхронного режима.
   */
  private AsyncProperties async = new AsyncProperties();

  /**
   * Флаг отключения предпросмотра веб-страниц в сообщениях.
   */
  private boolean disableWebPagePreview;

  /**
   * Режим парсинга сообщений (PLAIN, HTML, MARKDOWN). По умолчанию {@link ParseMode#PLAIN}.
   */
  private ParseMode parseMode = ParseMode.PLAIN;

  /**
   * Политика обработки ошибок при отправке уведомлений. По умолчанию {@link ErrorPolicy#LOG_ONLY}.
   */
  private ErrorPolicy errorPolicy = ErrorPolicy.LOG_ONLY;

  /**
   * Базовый размер пула потоков для исполнителя задач. По умолчанию 1.
   */
  private int executorCorePoolSize = 1;

  /**
   * Максимальный размер пула потоков для исполнителя задач. По умолчанию 1.
   */
  private int executorMaxPoolSize = 1;

  /**
   * Емкость очереди задач для исполнителя. По умолчанию 100.
   */
  private int executorQueueCapacity = 100;

  /**
   * Проверяет, включен ли уведомитель.
   *
   * @return {@code true}, если уведомитель включен, иначе {@code false}
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Устанавливает состояние включения/выключения уведомителя.
   *
   * @param enabled {@code true} для включения, {@code false} для выключения
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Получает токен Telegram-бота.
   *
   * <p>Если токен задан напрямую, возвращает его. Иначе пытается получить
   * из вложенных свойств {@link BotProperties}.
   *
   * @return токен бота или {@code null}, если не задан
   */
  public String getToken() {
    if (hasText(token)) {
      return token;
    }
    return bot != null ? bot.getToken() : null;
  }

  /**
   * Устанавливает токен Telegram-бота.
   *
   * @param token токен бота
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * Получает имя пользователя (username) Telegram-бота.
   *
   * <p>Если имя задано напрямую, возвращает его. Иначе пытается получить
   * из вложенных свойств {@link BotProperties}.
   *
   * @return имя пользователя бота или {@code null}, если не задано
   */
  public String getUsername() {
    if (hasText(username)) {
      return username;
    }
    return bot != null ? bot.getUsername() : null;
  }

  /**
   * Устанавливает имя пользователя (username) Telegram-бота.
   *
   * @param username имя пользователя бота
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Получает список идентификаторов чатов для отправки уведомлений.
   *
   * <p>Если список задан напрямую и не пуст, возвращает его. Иначе пытается получить
   * из вложенных свойств {@link TargetsProperties}.
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
   * Проверяет, включен ли асинхронный режим отправки уведомлений.
   *
   * <p>Если флаг установлен напрямую в {@code true}, возвращает {@code true}.
   * Иначе проверяет значение из вложенных свойств {@link AsyncProperties}.
   *
   * @return {@code true}, если асинхронный режим включен, иначе {@code false}
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
   * Устанавливает состояние включения/выключения асинхронного режима.
   *
   * @param asyncEnabled {@code true} для включения асинхронного режима
   */
  public void setAsyncEnabled(boolean asyncEnabled) {
    this.asyncEnabled = asyncEnabled;
  }

  /**
   * Получает вложенные свойства конфигурации бота.
   *
   * @return объект {@link BotProperties} с настройками бота
   */
  public BotProperties getBot() {
    return bot;
  }

  /**
   * Устанавливает вложенные свойства конфигурации бота.
   *
   * @param bot объект {@link BotProperties} с настройками бота
   */
  public void setBot(BotProperties bot) {
    this.bot = bot;
  }

  /**
   * Получает вложенные свойства конфигурации целевых чатов.
   *
   * @return объект {@link TargetsProperties} с настройками целевых чатов
   */
  public TargetsProperties getTargets() {
    return targets;
  }

  /**
   * Устанавливает вложенные свойства конфигурации целевых чатов.
   *
   * @param targets объект {@link TargetsProperties} с настройками целевых чатов
   */
  public void setTargets(TargetsProperties targets) {
    this.targets = targets;
  }

  /**
   * Получает вложенные свойства конфигурации асинхронного режима.
   *
   * @return объект {@link AsyncProperties} с настройками асинхронного режима
   */
  public AsyncProperties getAsync() {
    return async;
  }

  /**
   * Устанавливает вложенные свойства конфигурации асинхронного режима.
   *
   * @param async объект {@link AsyncProperties} с настройками асинхронного режима
   */
  public void setAsync(AsyncProperties async) {
    this.async = async;
  }

  /**
   * Проверяет, отключен ли предпросмотр веб-страниц в сообщениях.
   *
   * @return {@code true}, если предпросмотр отключен, иначе {@code false}
   */
  public boolean isDisableWebPagePreview() {
    return disableWebPagePreview;
  }

  /**
   * Устанавливает состояние отключения предпросмотра веб-страниц в сообщениях.
   *
   * @param disableWebPagePreview {@code true} для отключения предпросмотра
   */
  public void setDisableWebPagePreview(boolean disableWebPagePreview) {
    this.disableWebPagePreview = disableWebPagePreview;
  }

  /**
   * Получает режим парсинга сообщений.
   *
   * @return режим парсинга ({@link ParseMode#PLAIN}, {@link ParseMode#HTML} или
   *     {@link ParseMode#MARKDOWN})
   */
  public ParseMode getParseMode() {
    return parseMode;
  }

  /**
   * Устанавливает режим парсинга сообщений.
   *
   * @param parseMode режим парсинга
   */
  public void setParseMode(ParseMode parseMode) {
    this.parseMode = parseMode;
  }

  /**
   * Получает политику обработки ошибок при отправке уведомлений.
   *
   * @return политика обработки ошибок
   */
  public ErrorPolicy getErrorPolicy() {
    return errorPolicy;
  }

  /**
   * Устанавливает политику обработки ошибок при отправке уведомлений.
   *
   * @param errorPolicy политика обработки ошибок
   */
  public void setErrorPolicy(ErrorPolicy errorPolicy) {
    this.errorPolicy = errorPolicy;
  }

  /**
   * Получает базовый размер пула потоков для исполнителя задач.
   *
   * @return базовый размер пула потоков
   */
  public int getExecutorCorePoolSize() {
    return executorCorePoolSize;
  }

  /**
   * Устанавливает базовый размер пула потоков для исполнителя задач.
   *
   * @param executorCorePoolSize базовый размер пула потоков
   */
  public void setExecutorCorePoolSize(int executorCorePoolSize) {
    this.executorCorePoolSize = executorCorePoolSize;
  }

  /**
   * Получает максимальный размер пула потоков для исполнителя задач.
   *
   * @return максимальный размер пула потоков
   */
  public int getExecutorMaxPoolSize() {
    return executorMaxPoolSize;
  }

  /**
   * Устанавливает максимальный размер пула потоков для исполнителя задач.
   *
   * @param executorMaxPoolSize максимальный размер пула потоков
   */
  public void setExecutorMaxPoolSize(int executorMaxPoolSize) {
    this.executorMaxPoolSize = executorMaxPoolSize;
  }

  /**
   * Получает емкость очереди задач для исполнителя.
   *
   * @return емкость очереди задач
   */
  public int getExecutorQueueCapacity() {
    return executorQueueCapacity;
  }

  /**
   * Устанавливает емкость очереди задач для исполнителя.
   *
   * @param executorQueueCapacity емкость очереди задач
   */
  public void setExecutorQueueCapacity(int executorQueueCapacity) {
    this.executorQueueCapacity = executorQueueCapacity;
  }

  /**
   * Проверяет, содержит ли строка текстовое содержимое.
   *
   * <p>Строка считается имеющей текст, если она не {@code null} и после обрезки
   * пробелов не является пустой.
   *
   * @param value проверяемая строка
   * @return {@code true}, если строка содержит текст, иначе {@code false}
   */
  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  /**
   * Вложенные свойства конфигурации Telegram-бота.
   *
   * <p>Содержит основные параметры бота: токен и имя пользователя.
   */
  public static class BotProperties {
    /**
     * Токен Telegram-бота для аутентификации в API.
     */
    private String token;

    /**
     * Имя пользователя (username) Telegram-бота.
     */
    private String username;

    /**
     * Получает токен Telegram-бота.
     *
     * @return токен бота
     */
    public String getToken() {
      return token;
    }

    /**
     * Устанавливает токен Telegram-бота.
     *
     * @param token токен бота
     */
    public void setToken(String token) {
      this.token = token;
    }

    /**
     * Получает имя пользователя (username) Telegram-бота.
     *
     * @return имя пользователя бота
     */
    public String getUsername() {
      return username;
    }

    /**
     * Устанавливает имя пользователя (username) Telegram-бота.
     *
     * @param username имя пользователя бота
     */
    public void setUsername(String username) {
      this.username = username;
    }
  }

  /**
   * Вложенные свойства конфигурации целевых чатов.
   *
   * <p>Содержит список идентификаторов чатов, в которые будут отправляться уведомления.
   */
  public static class TargetsProperties {
    /**
     * Список идентификаторов чатов для отправки уведомлений.
     */
    private List<Long> chatIds = new ArrayList<>();

    /**
     * Получает список идентификаторов чатов для отправки уведомлений.
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
   * Вложенные свойства конфигурации асинхронного режима.
   *
   * <p>Определяет, должны ли уведомления отправляться асинхронно.
   */
  public static class AsyncProperties {
    /**
     * Флаг включения асинхронного режима отправки уведомлений.
     */
    private boolean enabled;

    /**
     * Проверяет, включен ли асинхронный режим отправки уведомлений.
     *
     * @return {@code true}, если асинхронный режим включен, иначе {@code false}
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Устанавливает состояние включения/выключения асинхронного режима.
     *
     * @param enabled {@code true} для включения асинхронного режима
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }
}
