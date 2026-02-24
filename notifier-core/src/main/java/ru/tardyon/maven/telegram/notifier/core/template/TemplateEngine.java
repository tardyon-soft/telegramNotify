package ru.tardyon.maven.telegram.notifier.core.template;

import ru.tardyon.maven.telegram.notifier.core.aop.MethodInvocationContext;

/**
 * Движок для рендеринга шаблонов сообщений уведомлений.
 *
 * <p>Используется для обработки строковых шаблонов с подстановкой значений
 * из контекста выполнения метода (параметры, результат, исключение и т.д.).
 */
public interface TemplateEngine {
  /**
   * Выполняет рендеринг шаблона с использованием данных из контекста.
   *
   * @param template строка-шаблон, содержащая выражения для подстановки
   *                 (например, SpEL-выражения вида "'result=' + #result")
   * @param context  контекст вызова метода с доступом к параметрам, результату и исключению
   * @return отрендеренная строка с подставленными значениями
   */
  String render(String template, MethodInvocationContext context);
}
