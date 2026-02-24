package ru.wildred.telegram.notifier.springaop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import ru.wildred.telegram.notifier.core.annotation.NotifyWhen;
import ru.wildred.telegram.notifier.core.annotation.TelegramNotify;
import ru.wildred.telegram.notifier.core.aop.MethodInvocationContext;
import ru.wildred.telegram.notifier.core.dispatch.NotificationRequest;
import ru.wildred.telegram.notifier.core.dispatch.TelegramNotificationDispatcher;

@Aspect
public class TelegramNotifyAspect {
    private final TelegramNotificationDispatcher dispatcher;

    public TelegramNotifyAspect(TelegramNotificationDispatcher dispatcher) {
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
    }

    @Around("@annotation(ann)")
    public Object around(ProceedingJoinPoint joinPoint, TelegramNotify ann) throws Throwable {
        Method method = resolveMethod(joinPoint);
        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();

        if (ann.when() == NotifyWhen.BEFORE) {
            dispatch(ann, target, method, args, null, null);
        }

        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            if (ann.when() == NotifyWhen.AFTER_SUCCESS) {
                dispatch(ann, target, method, args, result, null);
            }
            return result;
        } catch (Throwable ex) {
            error = ex;
            if (ann.when() == NotifyWhen.AFTER_FAILURE) {
                dispatch(ann, target, method, args, null, ex);
            }
            throw ex;
        } finally {
            if (ann.when() == NotifyWhen.AFTER_FINALLY) {
                dispatch(ann, target, method, args, result, error);
            }
        }
    }

    private void dispatch(
            TelegramNotify ann,
            Object target,
            Method method,
            Object[] args,
            Object result,
            Throwable throwable
    ) {
        MethodInvocationContext context = new MethodInvocationContext(target, method, args, result, throwable);
        NotificationRequest request = NotificationRequest.of(
                ann.message(),
                ann.condition(),
                toChatIds(ann.chatIds()),
                ann.parseMode(),
                ann.errorPolicy(),
                context
        );
        dispatcher.dispatch(request);
    }

    private List<Long> toChatIds(long[] chatIds) {
        if (chatIds == null || chatIds.length == 0) {
            return null;
        }
        List<Long> converted = new ArrayList<Long>(chatIds.length);
        for (long chatId : chatIds) {
            converted.add(chatId);
        }
        return converted;
    }

    private Method resolveMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method sourceMethod = signature.getMethod();
        Object target = joinPoint.getTarget();
        if (target == null) {
            return sourceMethod;
        }

        Class<?> targetClass = target.getClass();
        try {
            return targetClass.getMethod(sourceMethod.getName(), sourceMethod.getParameterTypes());
        } catch (NoSuchMethodException ignored) {
            try {
                Method declared = targetClass.getDeclaredMethod(sourceMethod.getName(), sourceMethod.getParameterTypes());
                declared.setAccessible(true);
                return declared;
            } catch (NoSuchMethodException secondIgnored) {
                return sourceMethod;
            }
        }
    }
}
