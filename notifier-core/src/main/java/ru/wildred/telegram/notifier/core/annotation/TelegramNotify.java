package ru.wildred.telegram.notifier.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ru.wildred.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.wildred.telegram.notifier.core.dispatch.ParseMode;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TelegramNotify {
    String message();

    String condition() default "";

    NotifyWhen when() default NotifyWhen.AFTER_SUCCESS;

    long[] chatIds() default {};

    ParseMode parseMode() default ParseMode.PLAIN;

    ErrorPolicy errorPolicy() default ErrorPolicy.LOG_ONLY;
}
