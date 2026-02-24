package ru.tardyon.maven.telegram.notifier.boot2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

/**
 * Условие для включения автоконфигурации Telegram-уведомлений.
 *
 * <p>Проверяет наличие всех необходимых параметров конфигурации:
 * <ul>
 *   <li>telegram.notifier.enabled = true</li>
 *   <li>telegram.notifier.token - токен бота</li>
 *   <li>telegram.notifier.username - имя пользователя бота</li>
 *   <li>chat-ids - список идентификаторов чатов (хотя бы один)</li>
 * </ul>
 *
 * <p>Автоконфигурация активируется только если все условия выполнены.
 */
public class TelegramNotifierEnabledCondition implements Condition {
  /**
   * Проверяет выполнение условий для включения автоконфигурации.
   *
   * <p>Метод загружает параметры конфигурации из окружения и проверяет:
   * <ul>
   *   <li>Флаг enabled установлен в true</li>
   *   <li>Указан токен бота</li>
   *   <li>Указано имя пользователя бота</li>
   *   <li>Указан хотя бы один идентификатор чата</li>
   * </ul>
   *
   * @param context  контекст условия с доступом к окружению
   * @param metadata метаданные аннотированного типа
   * @return true если все условия выполнены, false в противном случае
   */
  @Override
  public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
    Binder binder = Binder.get(context.getEnvironment());
    TelegramNotifierProperties props = binder.bind("telegram.notifier",
            Bindable.of(TelegramNotifierProperties.class))
        .orElseGet(TelegramNotifierProperties::new);

    boolean enabled = props.isEnabled();
    String token = props.getToken();
    String username = props.getUsername();
    List<Long> chatIds = resolveChatIds(binder, props);

    return enabled && hasText(token) && hasText(username) && !chatIds.isEmpty();
  }

  /**
   * Извлекает список идентификаторов чатов из различных источников конфигурации.
   *
   * <p>Пытается получить chat-ids в следующем порядке приоритета:
   * <ol>
   *   <li>Список из {@link TelegramNotifierProperties#getChatIds()}</li>
   *   <li>Строка из telegram.notifier.targets.chat-ids</li>
   *   <li>Строка из telegram.notifier.chat-ids</li>
   *   <li>Одиночное значение Long из telegram.notifier.targets.chat-id</li>
   *   <li>Одиночное значение Long из telegram.notifier.chat-id</li>
   *   <li>Строка из telegram.notifier.targets.chat-id (парсится как CSV)</li>
   *   <li>Строка из telegram.notifier.chat-id (парсится как CSV)</li>
   * </ol>
   *
   * @param binder объект для связывания свойств конфигурации
   * @param props  объект с настройками Telegram-уведомлений
   * @return список идентификаторов чатов, либо пустой список если не найдены
   */
  private List<Long> resolveChatIds(Binder binder, TelegramNotifierProperties props) {
    List<Long> fromList = props.getChatIds();
    if (fromList != null && !fromList.isEmpty()) {
      return fromList;
    }

    String fromString = binder.bind("telegram.notifier.targets.chat-ids", String.class).orElse("");
    if (!hasText(fromString)) {
      fromString = binder.bind("telegram.notifier.chat-ids", String.class).orElse("");
    }
    if (hasText(fromString)) {
      return parseCsv(fromString);
    }

    Long fromSingle = binder.bind("telegram.notifier.targets.chat-id", Long.class).orElse(null);
    if (fromSingle == null) {
      fromSingle = binder.bind("telegram.notifier.chat-id", Long.class).orElse(null);
    }
    if (fromSingle != null) {
      return Collections.singletonList(fromSingle);
    }

    String fromSingleString = binder.bind("telegram.notifier.targets.chat-id", String.class)
        .orElse("");
    if (!hasText(fromSingleString)) {
      fromSingleString = binder.bind("telegram.notifier.chat-id", String.class).orElse("");
    }
    if (hasText(fromSingleString)) {
      return parseCsv(fromSingleString);
    }

    return Collections.emptyList();
  }

  /**
   * Парсит строку со значениями, разделенными запятыми, в список Long.
   *
   * <p>Удаляет пробелы вокруг значений и игнорирует пустые элементы.
   *
   * @param value строка со значениями, разделенными запятыми
   * @return список числовых идентификаторов
   * @throws NumberFormatException если строка содержит нечисловое значение
   */
  private List<Long> parseCsv(String value) {
    return java.util.Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(this::hasText)
        .map(Long::valueOf)
        .collect(Collectors.toList());
  }

  /**
   * Проверяет, содержит ли строка непустой текст.
   *
   * @param value проверяемая строка
   * @return true если строка не null и содержит хотя бы один непробельный символ
   */
  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
