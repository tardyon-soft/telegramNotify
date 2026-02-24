package ru.wildred.telegram.notifier.core.aop;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public final class MethodInvocationContext {
    private final Object target;
    private final Method method;
    private final Object[] arguments;
    private final Object result;
    private final Throwable throwable;

    public MethodInvocationContext(
            Object target,
            Method method,
            Object[] arguments,
            Object result,
            Throwable throwable
    ) {
        this.target = target;
        this.method = method;
        this.arguments = arguments == null ? new Object[0] : Arrays.copyOf(arguments, arguments.length);
        this.result = result;
        this.throwable = throwable;
    }

    public Object target() {
        return target;
    }

    public Method method() {
        return method;
    }

    public Object[] arguments() {
        return Arrays.copyOf(arguments, arguments.length);
    }

    public Object result() {
        return result;
    }

    public Throwable throwable() {
        return throwable;
    }

    public static MethodInvocationContext empty() {
        return new MethodInvocationContext(null, null, new Object[0], null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodInvocationContext)) {
            return false;
        }
        MethodInvocationContext that = (MethodInvocationContext) o;
        return Objects.equals(target, that.target)
                && Objects.equals(method, that.method)
                && Arrays.equals(arguments, that.arguments)
                && Objects.equals(result, that.result)
                && Objects.equals(throwable, that.throwable);
    }

    @Override
    public int hashCode() {
        int result1 = Objects.hash(target, method, result, throwable);
        result1 = 31 * result1 + Arrays.hashCode(arguments);
        return result1;
    }
}
