package ru.tardyon.maven.telegram.notifier.boot3;

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
 * Условие для активации автоконфигурации Telegram-нотификатора.
 *
 * <p>Проверяет наличие всех обязательных параметров конфигурации:
 * <ul>
 *   <li>{@code telegram.notifier.enabled} - флаг включения (должен быть true)</li>
 *   <li>{@code telegram.notifier.token} - токен бота (обязательно)</li>
 *   <li>{@code telegram.notifier.username} - имя бота (обязательно)</li>
 *   <li>Идентификаторы чатов - хотя бы один chat-id (обязательно)</li>
 * </ul>
 *
 * <p>Поддерживает различные способы задания chat-id:
 * <ul>
 *   <li>Список: {@code telegram.notifier.chatIds} или
 *   {@code telegram.notifier.targets.chat-ids}</li>
 *   <li>CSV-строка: {@code telegram.notifier.chat-ids} или
 *   {@code telegram.notifier.targets.chat-ids}</li>
 *   <li>Одиночное значение: {@code telegram.notifier.chat-id} или
 *   {@code telegram.notifier.targets.chat-id}</li>
 * </ul>
 */

public class TelegramNotifierEnabledCondition implements Condition {
  /**
   * Проверяет выполнение условия для активации конфигурации.
   *
   * <p>Условие считается выполненным, если:
   * <ul>
   *   <li>Флаг {@code enabled} установлен в true</li>
   *   <li>Указан токен бота</li>
   *   <li>Указано имя бота</li>
   *   <li>Задан хотя бы один идентификатор чата</li>
   * </ul>
   *
   * @param context  контекст условия с доступом к окружению и реестру бинов
   * @param metadata метаданные аннотированного типа
   * @return {@code true} если все обязательные параметры заданы, иначе {@code false}
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
   * Разрешает идентификаторы чатов из различных источников конфигурации.
   *
   * <p>Порядок приоритета источников:
   * <ol>
   *   <li>Список из свойства {@code chatIds} объекта {@link TelegramNotifierProperties}</li>
   *   <li>CSV-строка из {@code telegram.notifier.targets.chat-ids}</li>
   *   <li>CSV-строка из {@code telegram.notifier.chat-ids}</li>
   *   <li>Одиночное значение Long из {@code telegram.notifier.targets.chat-id}</li>
   *   <li>Одиночное значение Long из {@code telegram.notifier.chat-id}</li>
   *   <li>CSV-строка из {@code telegram.notifier.targets.chat-id}</li>
   *   <li>CSV-строка из {@code telegram.notifier.chat-id}</li>
   * </ol>
   *
   * @param binder связыватель свойств Spring
   * @param props  свойства Telegram-нотификатора
   * @return список идентификаторов чатов или пустой список, если ничего не найдено
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
   * Парсит строку с идентификаторами чатов в формате CSV.
   *
   * <p>Разделяет строку по запятым, удаляет пробелы, отфильтровывает пустые значения
   * и преобразует каждое значение в {@link Long}.
   *
   * @param value строка с идентификаторами, разделёнными запятыми
   * @return список идентификаторов чатов
   * @throws NumberFormatException если какое-либо значение не может быть преобразовано в Long
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
   * <p>Строка считается непустой, если она не {@code null}
   * и содержит хотя бы один непробельный символ.
   *
   * @param value проверяемая строка
   * @return {@code true} если строка содержит текст, иначе {@code false}
   */
  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
