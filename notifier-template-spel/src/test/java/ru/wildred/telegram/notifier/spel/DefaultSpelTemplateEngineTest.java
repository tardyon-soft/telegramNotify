package ru.wildred.telegram.notifier.spel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import ru.wildred.telegram.notifier.core.aop.MethodInvocationContext;

class DefaultSpelTemplateEngineTest {

    private final DefaultSpelTemplateEngine engine = new DefaultSpelTemplateEngine();

    @Test
    void messageUsesP0AndResult() throws Exception {
        Method method = Samples.class.getDeclaredMethod("sum", String.class, int.class);
        MethodInvocationContext context = new MethodInvocationContext(
                new Samples(),
                method,
                new Object[]{"order", 3},
                7,
                null
        );

        String message = engine.render("#p0 + ':' + #result", context);

        assertEquals("order:7", message);
    }

    @Test
    void conditionExpressionIsEvaluated() {
        MethodInvocationContext contextTrue = MethodInvocationContext.empty();
        MethodInvocationContext contextFalse = MethodInvocationContext.empty();

        assertTrue(engine.evaluateCondition("1 < 2", contextTrue));
        assertFalse(engine.evaluateCondition("1 > 2", contextFalse));
    }

    @Test
    void namedParametersAreAvailableWithParametersFlag() throws Exception {
        Method method = Samples.class.getDeclaredMethod("sum", String.class, int.class);
        MethodInvocationContext context = new MethodInvocationContext(
                new Samples(),
                method,
                new Object[]{"item", 11},
                null,
                null
        );

        String message = engine.render("#name + '-' + #amount", context);

        assertEquals("item-11", message);
    }

    @Test
    void emptyConditionIsTrueAndEmptyMessageIsEmptyString() {
        assertTrue(engine.evaluateCondition("   ", MethodInvocationContext.empty()));
        assertEquals("", engine.render("   ", MethodInvocationContext.empty()));
    }

    private static final class Samples {
        @SuppressWarnings("unused")
        public int sum(String name, int amount) {
            return amount;
        }
    }
}
