package ru.tardyon.maven.telegram.notifier.spel;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import ru.tardyon.maven.telegram.notifier.core.aop.MethodInvocationContext;
import ru.tardyon.maven.telegram.notifier.core.template.TemplateEngine;

/**
 * Реализация шаблонизатора по умолчанию, использующая Spring Expression Language (SpEL).
 *
 * <p>Предоставляет возможность рендеринга шаблонов сообщений и вычисления условий
 * на основе контекста выполнения метода. Поддерживает кэширование скомпилированных
 * выражений для повышения производительности.</p>
 */

public class DefaultSpelTemplateEngine implements TemplateEngine {
  private final ExpressionParser expressionParser;
  private final ParameterNameDiscoverer parameterNameDiscoverer;
  private final ConcurrentMap<String, Expression> expressionCache;

  /**
   * Создает экземпляр шаблонизатора с настройками по умолчанию.
   *
   * <p>Использует стандартный SpEL парсер, обнаружение имен параметров по умолчанию
   * и потокобезопасный кэш для хранения скомпилированных выражений.</p>
   */
  public DefaultSpelTemplateEngine() {
    this(new SpelExpressionParser(),
        new DefaultParameterNameDiscoverer(),
        new ConcurrentHashMap<>());
  }

  /**
   * Создает экземпляр шаблонизатора с заданными зависимостями.
   *
   * @param expressionParser
   *     парсер SpEL выражений
   * @param parameterNameDiscoverer
   *     обнаружитель имен параметров методов
   * @param expressionCache
   *     кэш для хранения скомпилированных выражений
   * @throws NullPointerException
   *     если любой из параметров равен null
   */
  DefaultSpelTemplateEngine(
      ExpressionParser expressionParser,
      ParameterNameDiscoverer parameterNameDiscoverer,
      ConcurrentMap<String, Expression> expressionCache
  ) {
    this.expressionParser = Objects
        .requireNonNull(expressionParser, "expressionParser");
    this.parameterNameDiscoverer = Objects
        .requireNonNull(parameterNameDiscoverer, "parameterNameDiscoverer");
    this.expressionCache = Objects
        .requireNonNull(expressionCache, "expressionCache");
  }

  /**
   * Рендерит шаблон сообщения, используя контекст выполнения метода.
   *
   * <p>В контексте доступны следующие переменные:
   * <ul>
   *   <li>{@code #args} - массив всех аргументов метода</li>
   *   <li>{@code #p0, #p1, ...} или {@code #a0, #a1, ...} - аргументы по индексу</li>
   *   <li>{@code #paramName} - аргументы по имени (если доступны)</li>
   *   <li>{@code #result} - результат выполнения метода</li>
   *   <li>{@code #ex} - исключение, если было брошено</li>
   *   <li>{@code #methodName} - имя метода</li>
   *   <li>{@code #className} - имя класса</li>
   * </ul></p>
   *
   * @param template
   *     шаблон SpEL выражения
   * @param context
   *     контекст выполнения метода
   * @return отрендеренное сообщение или пустая строка, если шаблон пуст или результат null
   */
  @Override
  public String render(String template, MethodInvocationContext context) {
    if (isBlank(template)) {
      return "";
    }

    Object value = evaluateInternal(template, safeContext(context));
    return value == null ? "" : String.valueOf(value);
  }

  /**
   * Вычисляет условие отправки уведомления на основе контекста выполнения метода.
   *
   * <p>Доступны те же переменные, что и в {@link #render(String, MethodInvocationContext)}.</p>
   *
   * @param conditionTemplate
   *     шаблон SpEL выражения для условия
   * @param context
   *     контекст выполнения метода
   * @return {@code true}, если шаблон пуст, или результат вычисления условия как boolean;
   *     {@code false}, если результат null или не может быть преобразован в {@code true}
   *
   */
  public boolean evaluateCondition(String conditionTemplate, MethodInvocationContext context) {
    if (isBlank(conditionTemplate)) {
      return true;
    }

    Object value = evaluateInternal(conditionTemplate, safeContext(context));
    if (value == null) {
      return false;
    }
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return Boolean.parseBoolean(String.valueOf(value).trim());
  }

  /**
   * Внутренний метод для вычисления SpEL выражения с кэшированием.
   *
   * @param expression
   *     строка SpEL выражения
   * @param context
   *     контекст выполнения метода
   * @return результат вычисления выражения
   */
  private Object evaluateInternal(String expression, MethodInvocationContext context) {
    Expression parsedExpression = expressionCache
        .computeIfAbsent(expression, expressionParser::parseExpression);
    return parsedExpression.getValue(createEvaluationContext(context));
  }

  /**
   * Создает контекст вычисления SpEL выражений на основе контекста выполнения метода.
   *
   * <p>Регистрирует следующие переменные:
   * <ul>
   *   <li>{@code args} - массив всех аргументов</li>
   *   <li>{@code p0, p1, ...} и {@code a0, a1, ...} - аргументы по индексу</li>
   *   <li>имена параметров - аргументы по имени (если доступны)</li>
   *   <li>{@code result} - результат выполнения</li>
   *   <li>{@code ex} - исключение</li>
   *   <li>{@code methodName} - имя метода</li>
   *   <li>{@code className} - имя класса</li>
   * </ul></p>
   *
   * @param context
   *     контекст выполнения метода
   * @return настроенный контекст вычисления
   */
  private EvaluationContext createEvaluationContext(MethodInvocationContext context) {
    StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    Object[] args = context.arguments();
    evaluationContext.setVariable("args", args);
    for (int i = 0; i < args.length; i++) {
      evaluationContext.setVariable("p" + i, args[i]);
      evaluationContext.setVariable("a" + i, args[i]);
    }

    evaluationContext.setVariable("result", context.result());
    evaluationContext.setVariable("ex", context.throwable());

    Method method = context.method();
    if (method != null) {
      evaluationContext.setVariable("methodName", method.getName());
      evaluationContext.setVariable("className", method.getDeclaringClass().getName());

      String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
      if (parameterNames != null) {
        int limit = Math.min(parameterNames.length, args.length);
        for (int i = 0; i < limit; i++) {
          evaluationContext.setVariable(parameterNames[i], args[i]);
        }
      }
    } else {
      evaluationContext.setVariable("methodName", null);
      evaluationContext.setVariable("className",
          context.target() == null ? null : context.target().getClass().getName());
    }

    return evaluationContext;
  }

  /**
   * Возвращает безопасный контекст выполнения метода.
   *
   * @param context
   *     контекст выполнения или null
   * @return переданный контекст или пустой контекст, если передан null
   */
  private MethodInvocationContext safeContext(MethodInvocationContext context) {
    return context == null ? MethodInvocationContext.empty() : context;
  }

  /**
   * Проверяет, является ли строка пустой или содержит только пробельные символы.
   *
   * @param value
   *     проверяемая строка
   * @return {@code true}, если строка null, пустая или содержит только пробелы
   */
  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
